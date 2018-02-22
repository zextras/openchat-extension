package com.zextras.modules.chat.server.db.sql;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.zextras.lib.ChatDbHelper;
import com.zextras.lib.sql.DbPrefetchIterator;
import com.zextras.lib.sql.QueryExecutor;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.db.DbHandler;
import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImMessageStatements
{
  private final static String sql_insert =
    "    INSERT INTO MESSAGE (" +
      "    ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    IS_MULTICHAT," +
      "    INDEX_STATUS," +
      "    TEXT," +
      "    SENDER," +
      "    DESTINATION," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO)" +
      "    VALUES(?,?,?,?,?,?,?,?,?,?,?)";
  private final static String sql_update =
    "    UPDATE MESSAGE " +
      "    SET SENT_TIMESTAMP = ?," +
      "    EDIT_TIMESTAMP = ?," +
      "    MESSAGE_TYPE = ?," +
      "    IS_MULTICHAT = ?," +
      "    INDEX_STATUS = ?," +
      "    TEXT = ?," +
      "    SENDER = ?," +
      "    DESTINATION = ?," +
      "    REACTIONS = ?," +
      "    TYPE_EXTRAINFO = ?," +
      "    WHERE ID = ?";
  private final static String sSELECT_MESSAGE =
    "    SELECT ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    IS_MULTICHAT," +
      "    INDEX_STATUS," +
      "    SENDER," +
      "    DESTINATION," +
      "    TEXT," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO" +
      "    FROM MESSAGE " +
      "    WHERE SENDER = ? ";

  private final static String sSELECT_MESSAGE_ORDER =
      "    ORDER BY SENT_TIMESTAMP ASC" +
        "    LIMIT ? OFFSET ?";

  private final static String sql_text =
    "    SELECT ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    IS_MULTICHAT," +
      "    INDEX_STATUS," +
      "    SENDER," +
      "    DESTINATION," +
      "    TEXT," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO" +
      "    FROM MESSAGE " +
      "    WHERE SENDER = ? " +
      "    AND DESTINATION = ? " +
      "    AND TEXT LIKE ? ESCAPE '!'" +
      "    ORDER BY SENT_TIMESTAMP ASC" +
      "    LIMIT ? OFFSET ?";
  private final static String sql_text_insensitive =
    "    SELECT ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    IS_MULTICHAT," +
      "    INDEX_STATUS," +
      "    SENDER," +
      "    DESTINATION," +
      "    TEXT, " +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO" +
      "    FROM MESSAGE " +
      "    WHERE SENDER = ? " +
      "    AND DESTINATION = ? " +
      "    AND TRANSLATE( LOWER(TEXT), 'áçéíóúàèìòùâêîôûãõëü', 'aceiouaeiouaeiouaoeu') LIKE TRANSLATE( ?, 'áçéíóúàèìòùâêîôûãõëü', 'aceiouaeiouaeiouaoeu') ESCAPE '!'" +
      "    ORDER BY SENT_TIMESTAMP ASC" +
      "    LIMIT ? OFFSET ?";

  private final static String sINSERT_MESSAGE_READ =
    "INSERT INTO MESSAGE_READ (SENDER,DESTINATION,TIMESTAMP,MESSAGE_ID) VALUES (?,?,?,?) ";

  private final static String sDELETE_MESSAGE_READ =
    "DELETE FROM MESSAGE_READ WHERE SENDER=? AND DESTINATION=?";

  private final static String sSELECT_MESSAGE_READ =
    "SELECT TIMESTAMP FROM MESSAGE_READ WHERE SENDER = ? AND DESTINATION = ?";

  private final static String sSELECT_ALL_MESSAGE_READ =
    "SELECT SENDER,TIMESTAMP FROM MESSAGE_READ WHERE DESTINATION = ?";

  private final static String sCOUNT_MESSAGE_TO_READ =
    "SELECT COUNT(*) FROM MESSAGE WHERE SENDER = ? AND DESTINATION = ? AND (SENT_TIMESTAMP > ? OR EDIT_TIMESTAMP > ?) ";

  private final static String sCOUNT_ALL_MESSAGE_TO_READ =
    "SELECT MESSAGE.SENDER,COUNT(MESSAGE.ID) " +
      " FROM MESSAGE " +
      " LEFT JOIN MESSAGE_READ " +
      " ON MESSAGE.SENDER = MESSAGE_READ.SENDER AND " +
      "    MESSAGE.DESTINATION = MESSAGE_READ.DESTINATION " +
      " WHERE MESSAGE.DESTINATION = ? " +
      " GROUP BY MESSAGE.SENDER";

  private final static String sALL_RECIPENTS =
    "SELECT MESSAGE.SENDER " +
      " FROM MESSAGE " +
      " WHERE MESSAGE.DESTINATION = ? " +
      " GROUP BY MESSAGE.SENDER";

  private final DbHandler mDbHandler;
  private final ChatDbHelper mChatDbHelper;
  private final MessageFactory mMessageFactory;

  @Inject
  public ImMessageStatements(
    DbHandler dbHandler,
    ChatDbHelper chatDbHelper,
    MessageFactory messageFactory
  )
  {
    mDbHandler = dbHandler;
    mChatDbHelper = chatDbHelper;
    mMessageFactory = messageFactory;
  }

  public void insert(ImMessage imMessage) throws SQLException
  {
    int i = 1;
    Connection connection = mDbHandler.getConnection();
    PreparedStatement statement = null;
    try
    {
      assertDBConnection(connection);
      statement = connection.prepareStatement(sql_insert);
      statement.setString(i++, imMessage.getId());
      statement.setLong(i++, imMessage.getSentTimestamp());
      statement.setLong(i++, imMessage.getEditTimestamp());
      statement.setShort(i++, EventType.toShort(imMessage.getMessageType()));
      statement.setBoolean(i++, imMessage.isMultichat());
      statement.setShort(i++, imMessage.getIndexStatus());
      statement.setString(i++, imMessage.getText());
      statement.setString(i++, imMessage.getSender());
      statement.setString(i++, imMessage.getDestination());
      statement.setString(i++, imMessage.getReactions());
      statement.setString(i++, imMessage.getTypeExtrainfo());
      statement.execute();
    }
    finally
    {
      DbUtils.closeQuietly(statement);
      DbUtils.closeQuietly(connection);
    }
  }

  public void update(ImMessage imMessage) throws SQLException
  {
    int i = 1;
    Connection connection = mDbHandler.getConnection();
    PreparedStatement statement = null;
    try
    {
      assertDBConnection(connection);
      statement = connection.prepareStatement(sql_update);
      statement.setLong(i++, imMessage.getSentTimestamp());
      statement.setLong(i++, imMessage.getEditTimestamp());
      statement.setShort(i++, EventType.toShort(imMessage.getMessageType()));
      statement.setBoolean(i++, imMessage.isMultichat());
      statement.setShort(i++, imMessage.getIndexStatus());
      statement.setString(i++, imMessage.getText());
      statement.setString(i++, imMessage.getSender());
      statement.setString(i++, imMessage.getDestination());
      statement.setString(i++, imMessage.getReactions());
      statement.setString(i++, imMessage.getTypeExtrainfo());
      statement.setString(i++, imMessage.getId());
      statement.execute();
    }
    finally
    {
      DbUtils.closeQuietly(statement);
      DbUtils.closeQuietly(connection);
    }
  }

  public DbPrefetchIterator<ImMessage> query(
    final String sender,
    final String destination,
    final Optional<Long> fromTime,
    final Optional<Long> toTime,
    final Optional<Integer> max) throws SQLException
  {
    return new DbPrefetchIterator<ImMessage>(new QueryExecutor()
    {
      Connection mConnection;

      @Override
      public ResultSet executeQuery(int start, int size) throws SQLException
      {
        mConnection = mDbHandler.getConnection();
        assertDBConnection(mConnection);
        StringBuilder sb = new StringBuilder(sSELECT_MESSAGE);
        if (!destination.isEmpty())
        {
          sb.append(" AND DESTINATION = ?");
        }
        if (fromTime.isPresent())
        {
          sb.append(" AND (SENT_TIMESTAMP >= ? OR EDIT_TIMESTAMP >= ?)");
        }
        if (toTime.isPresent())
        {
          sb.append(" AND (SENT_TIMESTAMP <= ? OR EDIT_TIMESTAMP <= ?)");
        }
        sb.append(sSELECT_MESSAGE_ORDER);

        PreparedStatement query = mConnection.prepareStatement(sb.toString());
        int i = 1;
        query.setString(i++, sender);
        if (!destination.isEmpty())
        {
          query.setString(i++, destination);
        }
        if (fromTime.isPresent())
        {
          query.setLong(i++, fromTime.get());
          query.setLong(i++, fromTime.get());
        }
        if (toTime.isPresent())
        {
          query.setLong(i++, toTime.get());
          query.setLong(i++, toTime.get());
        }
        query.setInt(i++, size);
        query.setInt(i++, start);
        return query.executeQuery();
      }

      @Override
      public void close()
      {
        DbUtils.closeQuietly(mConnection);
      }

    }, mMessageFactory,max.or(5000));
  }

  public DbPrefetchIterator<ImMessage> query(final String sender, final String destination, final String text) throws SQLException
  {
    return new DbPrefetchIterator<ImMessage>(new QueryExecutor()
    {
      Connection mConnection;

      @Override
      public ResultSet executeQuery(int start, int size) throws SQLException
      {
        mConnection = mDbHandler.getConnection();
        assertDBConnection(mConnection);
        PreparedStatement query = mConnection.prepareStatement(sql_text);
        query.setString(1, sender);
        query.setString(2, destination);
        query.setString(3, "%" + likeSanitize(text) + "%");
        query.setInt(4, size);
        query.setInt(5, start);
        return query.executeQuery();
      }

      @Override
      public void close()
      {
        DbUtils.closeQuietly(mConnection);
      }

    }, mMessageFactory,100);
  }

  public DbPrefetchIterator<ImMessage> queryInsensitive(final String sender, final String destination, final String text) throws SQLException
  {
    return new DbPrefetchIterator<ImMessage>(new QueryExecutor()
    {
      Connection mConnection;

      @Override
      public ResultSet executeQuery(int start, int size) throws SQLException
      {
        mConnection = mDbHandler.getConnection();
        assertDBConnection(mConnection);
        PreparedStatement query = mConnection.prepareStatement(sql_text_insensitive);
        query.setString(1, sender);
        query.setString(2, destination);
        query.setString(3, "%" + likeSanitize(text.toLowerCase()) + "%");
        query.setInt(4, size);
        query.setInt(5, start);
        return query.executeQuery();
      }

      @Override
      public void close()
      {
        DbUtils.closeQuietly(mConnection);
      }

    }, mMessageFactory,100);
  }

  public void upsertMessageRead(final String sender,final String destination,final long timestamp,final String id) throws SQLException
  {
    ChatDbHelper.DbConnection connection = mChatDbHelper.beginTransaction();
    try
    {
      mChatDbHelper.query(connection, sDELETE_MESSAGE_READ, new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          int i = 1;
          preparedStatement.setString(i++, sender);
          preparedStatement.setString(i++, destination);
        }
      });
      mChatDbHelper.query(connection, sINSERT_MESSAGE_READ, new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          int i = 1;
          preparedStatement.setString(i++, sender);
          preparedStatement.setString(i++, destination);
          preparedStatement.setLong(i++, timestamp);
          preparedStatement.setString(i++, id);
        }
      });
      connection.commitAndClose();
    }
    catch (SQLException e)
    {
      connection.rollbackAndClose();
      throw e;
    }
  }

  public long getLastMessageRead(final String sender, final String destination) throws SQLException
  {
    final long[] timestamp = {0};

    mChatDbHelper.query(sSELECT_MESSAGE_READ,
      new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          int i = 1;
          preparedStatement.setString(i++, sender);
          preparedStatement.setString(i++, destination);
        }
      },
      new ChatDbHelper.ResultSetFactory()
      {
        @Override
        public void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException
        {
          timestamp[0] = rs.getLong(1);
        }
      });

    return timestamp[0];
  }

  public Map<String,Long> getLastMessageRead(final String destination) throws SQLException
  {
    final Map<String,Long> map = new HashMap<String,Long>();

    mChatDbHelper.query(sSELECT_ALL_MESSAGE_READ,
      new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          int i = 1;
          preparedStatement.setString(i++, destination);
        }
      },
      new ChatDbHelper.ResultSetFactory()
      {
        @Override
        public void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException
        {
          map.put(rs.getString(1),rs.getLong(2));
        }
      });

    return map;
  }

  public Set<String> getAllRecipents(final String destination) throws SQLException
  {
    final Set<String> set = new HashSet<String>();

    mChatDbHelper.query(sALL_RECIPENTS,
      new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, destination);
        }
      },
      new ChatDbHelper.ResultSetFactory()
      {
        @Override
        public void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException
        {
          set.add(rs.getString(1));
        }
      });

    return set;
  }

  public Map<String,Pair<Integer,Long>> getCountMessageToRead(final String destination) throws SQLException
  {
    Map<String,Pair<Integer,Long>> map = new HashMap<String,Pair<Integer,Long>>();
    Set<String> recipients = getAllRecipents(destination);
    for (String sender : recipients)
    {
      long timestamp = getLastMessageRead(destination,sender);
      int count = getCountMessageToRead(sender,destination,timestamp);
      map.put(sender,Pair.<Integer,Long>of(count,timestamp));
    }
    return map;
  }

  public Map<String,Integer> getCountMessageToReadFast(final String destination) throws SQLException
  {
    final Map<String,Integer> map = new HashMap<String,Integer>();

    mChatDbHelper.query(sCOUNT_ALL_MESSAGE_TO_READ, // TODO: fix it
      new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, destination);
        }
      },
      new ChatDbHelper.ResultSetFactory()
      {
        @Override
        public void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException
        {
          map.put(rs.getString(1),rs.getInt(2));
        }
      });

    return map;
  }

  public int getCountMessageToRead(final String sender,final String destination,final long timestamp) throws SQLException
  {
    final int[] count = new int[1];
    mChatDbHelper.query(sCOUNT_MESSAGE_TO_READ,
      new ChatDbHelper.ParametersFactory()
      {
        @Override
        public void create(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, sender);
          preparedStatement.setString(2, destination);
          preparedStatement.setLong(3, timestamp);
          preparedStatement.setLong(4, timestamp);
        }
      },
      new ChatDbHelper.ResultSetFactory()
      {
        @Override
        public void create(ResultSet rs) throws SQLException, UnavailableResource, ChatDbException
        {
          count[0] = rs.getInt(1);
        }
      });

    return count[0];
  }

  private void assertDBConnection(Connection connection) throws SQLException
  {
    if (connection == null)
    {
      throw new SQLException("Error getting DB connection");
    }
  }

  public static String likeSanitize(String input)
  {
    return input
      .replace("!", "!!")
      .replace("%", "!%")
      .replace("_", "!_")
      .replace("[", "![");
  }
}
