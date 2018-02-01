package com.zextras.modules.chat.services;

import com.google.inject.Inject;
import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.db.mappers.StatementsFactory;
import com.zextras.modules.chat.server.relationship.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.db.sql.SqlClosure;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatSqlException;
import com.zextras.modules.chat.server.relationship.DirectRelationshipStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 This class is to preload relationships which needs to be very, very fast.
 It can also be used to load other information from chat database.
 Simply load all relationships and put them into DirectRelationshipStorage

 @see DirectRelationshipStorage
*/
public class DbPreloader implements Service
{
  private final DbHandler                 mDbHandler;
  private final DirectRelationshipStorage mDirectRelationshipStorage;
  private final StatementsFactory         mStatementsFactory;

  @Inject
  public DbPreloader(
    DbHandler dbHandler,
    DirectRelationshipStorage directRelationshipStorage,
    StatementsFactory statementsFactory
  )
  {
    mDbHandler = dbHandler;
    mDirectRelationshipStorage = directRelationshipStorage;
    mStatementsFactory = statementsFactory;
  }


  @Override
  public void start() throws ServiceStartException
  {
    try
    {
      new RelationshipsPreloadSqlClosure(mDbHandler).execute();
    }
    catch (ChatDbException e)
    {
      throw new ServiceStartException("Unable to preload relationships", e);
    }
  }

  @Override
  public void stop()
  {
  }

  private class RelationshipsPreloadSqlClosure extends SqlClosure<Void>
  {
    RelationshipsPreloadSqlClosure(DbHandler dbHandler)
    {
      super(dbHandler);
    }

    @Override
    public Void execute(Connection connection)
      throws ChatDbException
    {
      String sql = mStatementsFactory.buildSelectAllRelationship();

      PreparedStatement stmt = null;
      try
      {
        stmt = connection.prepareStatement(
          sql,
          Statement.RETURN_GENERATED_KEYS
        );

        ResultSet rs = null;
        try
        {
          rs = stmt.executeQuery();
          while (rs.next())
          {
            int userId = rs.getInt("USERID");
            mDirectRelationshipStorage.loadInitialValues(
              userId,
              new Relationship(
                new SpecificAddress(rs.getString("BUDDYADDRESS")),
                Relationship.RelationshipType.fromByte(rs.getByte("TYPE")),
                rs.getString("BUDDYNICKNAME"),
                rs.getString("GROUP")
              )
            );
          }
        }
        finally
        {
          if( rs != null)
          {
            rs.close();
          }
        }
      }
      catch (SQLException e)
      {
        ChatSqlException sqlException = new ChatSqlException(sql);
        sqlException.initCause(e);
        throw sqlException;
      }
      finally
      {
        try{ stmt.close(); }
        catch (SQLException ignore){}
      }

      return null;
    }
  }
}
