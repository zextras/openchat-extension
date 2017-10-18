package com.zextras.modules.chat.services;

import com.zextras.lib.switches.Service;
import com.zextras.modules.chat.server.Relationship;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.db.sql.SqlClosure;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.ChatSqlException;
import com.zextras.modules.chat.server.relationship.DirectRelationshipStorage;
import org.apache.commons.dbutils.DbUtils;

import javax.inject.Inject;
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

  @Inject
  public DbPreloader(
    DbHandler dbHandler,
    DirectRelationshipStorage directRelationshipStorage
  )
  {
    mDbHandler = dbHandler;
    mDirectRelationshipStorage = directRelationshipStorage;
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
    final static String sSql = "SELECT * FROM RELATIONSHIP";

    RelationshipsPreloadSqlClosure(DbHandler dbHandler)
    {
      super(dbHandler);
    }

    @Override
    public Void execute(Connection connection)
      throws ChatDbException
    {
      PreparedStatement stmt = null;
      try
      {
        stmt = connection.prepareStatement(
          sSql,
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
                new SpecificAddress(rs.getString("BUDDYADDRESS").intern()),
                Relationship.RelationshipType.fromByte(rs.getByte("TYPE")),
                rs.getString("BUDDYNICKNAME").intern(),
                rs.getString("GROUP").intern()
              )
            );
          }
        }
        finally
        {
          DbUtils.closeQuietly(rs);
        }
      }
      catch (SQLException e)
      {
        ChatSqlException sqlException = new ChatSqlException(sSql);
        sqlException.initCause(e);
        throw sqlException;
      }
      finally
      {
        DbUtils.closeQuietly(stmt);
      }

      return null;
    }
  }
}
