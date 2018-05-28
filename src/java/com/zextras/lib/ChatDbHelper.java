package com.zextras.lib;

import com.google.inject.Inject;
import com.zextras.lib.log.ChatLog;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.apache.commons.dbutils.DbUtils;
import org.openzal.zal.Utils;
import org.openzal.zal.log.ZimbraLog;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatDbHelper
{
  private final DbHandler mDbHandler;

  @Inject
  public ChatDbHelper(DbHandler dbHandler)
  {
    mDbHandler = dbHandler;
  }

  public static class DbConnection implements Closeable
  {
    private java.sql.Connection mConnection;
    private Boolean mOldAutoCommitState;

    private DbConnection(Connection connection) throws SQLException
    {
      if (connection == null)
      {
        throw new SQLException("Error getting DB connection");
      }
      mConnection = connection;
      mOldAutoCommitState = null;
    }

    public void close()
    {
      DbUtils.closeQuietly(mConnection);
    }

    public void beginTransaction() throws SQLException
    {
      try
      {
        mOldAutoCommitState = mConnection.getAutoCommit();
        mConnection.setAutoCommit(false);
      }
      catch (SQLException e)
      {
        DbUtils.closeQuietly(mConnection);
        throw e;
      }
    }

    public void commitAndClose() throws SQLException
    {
      if (mOldAutoCommitState == null)
      {
        throw new SQLException("BeginTransaction not called");
      }
      try
      {
        mConnection.commit();
      }
      finally
      {
        mConnection.setAutoCommit(mOldAutoCommitState);
        DbUtils.closeQuietly(mConnection);
      }
    }

    public void rollbackAndClose() throws SQLException
    {
      if (mOldAutoCommitState == null)
      {
        throw new SQLException("BeginTransaction not called");
      }
      try
      {
        mConnection.rollback();
      }
      finally
      {
        mConnection.setAutoCommit(mOldAutoCommitState);
        DbUtils.closeQuietly(mConnection);
      }
    }

    private PreparedStatement prepareStatement(String query) throws SQLException
    {
      return mConnection.prepareStatement(query);
    }
  }

  static class NoParameters implements ParametersFactory
  {
    @Override
    public void create(PreparedStatement preparedStatement) throws SQLException
    {
    }
  }

  public interface ResultSetFactory
  {
    void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException;
  }

  public interface ParametersFactory
  {
    void create(PreparedStatement preparedStatement) throws SQLException;
  }

  public DbConnection beginTransaction() throws SQLException
  {
    DbConnection dbConnection = new DbConnection(mDbHandler.getConnection());
    dbConnection.beginTransaction();
    return dbConnection;
  }

  // No transaction is started
  public DbConnection beginConnection() throws SQLException
  {
    return new DbConnection(mDbHandler.getConnection());
  }

  public void rollbackAndClose(DbConnection connection)
  {
    if (connection != null)
    {
      try
      {
        connection.rollbackAndClose();
      }
      catch( SQLException e )
      {
        ChatLog.log.err(Utils.exceptionToString(e));
      }
    }
  }

  public void query(String sql, ResultSetFactory rsFactory) throws SQLException
  {
    query(sql, new NoParameters() ,rsFactory);
  }

  public void query(String sql, ParametersFactory parametersFactory, ResultSetFactory rsFactory) throws SQLException
  {
    DbConnection connection = new DbConnection(mDbHandler.getConnection());
    try
    {
      executeQuery(connection,sql,parametersFactory,rsFactory);
    }
    finally
    {
      connection.close();
    }
  }

  public void query(DbConnection connection, String query,ParametersFactory parametersFactory) throws SQLException
  {
    executeQuery(connection,query, parametersFactory);
  }

  public void query(DbConnection connection, String query,ResultSetFactory rsFactory) throws SQLException
  {
    executeQuery(connection,query, new NoParameters(), rsFactory);
  }

  public void executeQuery(DbConnection connection, String query,ParametersFactory parametersFactory,ResultSetFactory rsFactory) throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      statement = connection.prepareStatement(query);
      parametersFactory.create(statement);
      rs = statement.executeQuery();
      while (rs.next())
      {
        try
        {
          rsFactory.create(rs);
        }
        catch (SQLException e)
        {
          ChatLog.log.err(Utils.exceptionToString(e));
        }
        catch (UnavailableResource unavailableResource)
        {
          ChatLog.log.err(Utils.exceptionToString(unavailableResource));
        }
        catch (ChatDbException e)
        {
          ChatLog.log.err(Utils.exceptionToString(e));
        }
      }
    }
    finally
    {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(statement);
    }
  }
  // begin and close transaction
  public int executeTransactionQuery(String query) throws SQLException
  {
    return executeTransactionQuery(query,new NoParameters());
  }

  // begin and close transaction
  public int executeTransactionQuery(String query, ParametersFactory parametersFactory) throws SQLException
  {
    DbConnection connection = beginTransaction();
    try
    {
      int res = executeQuery(connection,query,parametersFactory);
      connection.commitAndClose();
      return res;
    }
    catch (SQLException e)
    {
      connection.rollbackAndClose();
      throw e;
    }
  }

  // require connection.commit and connection.close or connection.rollBackAndClose
  public int executeQuery(DbConnection connection, String query) throws SQLException
  {
    return executeQuery(connection,query,new NoParameters());
  }

  // require connection.commit and connection.close or connection.rollBackAndClose
  public int executeQuery(DbConnection connection, String query, ParametersFactory parametersFactory) throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      statement = connection.prepareStatement(query);
      parametersFactory.create(statement);
      return statement.executeUpdate();
    }
    finally
    {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(statement);
    }
  }
}
