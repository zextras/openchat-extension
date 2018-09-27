package com.zextras.modules.chat.server.db.sql;

import com.zextras.lib.Container;
import com.zextras.lib.ContainerImpl;
import com.zextras.lib.ContainerListContainer;
import com.zextras.lib.Optional;
import com.google.inject.Inject;
import com.zextras.lib.DbHelper;
import com.zextras.modules.chat.server.ImMessage;
import com.zextras.modules.chat.server.address.SubdomainResolver;
import com.zextras.modules.chat.server.db.ChatDbHandler;
import com.zextras.modules.chat.server.events.EventType;
import com.zextras.modules.chat.server.events.TargetType;
import com.zextras.modules.chat.server.exceptions.ChatDbException;
import com.zextras.modules.chat.server.exceptions.UnavailableResource;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ImMessageStatements
{
  private final static String sql_insert =
    "    INSERT INTO chat.MESSAGE (" +
      "    ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    TARGET_TYPE," +
      "    INDEX_STATUS," +
      "    TEXT," +
      "    SENDER," +
      "    DESTINATION," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO)" +
      "    VALUES(?,?,?,?,?,?,?,?,?,?,?)";
  private final static String sql_update =
    "    UPDATE chat.MESSAGE " +
      "    SET SENT_TIMESTAMP = ?," +
      "    EDIT_TIMESTAMP = ?," +
      "    MESSAGE_TYPE = ?," +
      "    TARGET_TYPE = ?," +
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
      "    TARGET_TYPE," +
      "    INDEX_STATUS," +
      "    SENDER," +
      "    DESTINATION," +
      "    TEXT," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO" +
      "    FROM chat.MESSAGE ";

  private final static String sSELECT_MESSAGE_ORDER =
      "    ORDER BY SENT_TIMESTAMP DESC" + // TODO: EDIT_TIMESTAMP
        "    LIMIT ?";

  private final static String sql_text =
    "    SELECT ID," +
      "    SENT_TIMESTAMP," +
      "    EDIT_TIMESTAMP," +
      "    MESSAGE_TYPE," +
      "    INDEX_STATUS," +
      "    SENDER," +
      "    DESTINATION," +
      "    TEXT," +
      "    REACTIONS," +
      "    TYPE_EXTRAINFO" +
      "    FROM chat.MESSAGE " +
      "    WHERE SENDER = ? " +
      "    AND DESTINATION = ? " +
      "    AND TEXT LIKE ? ESCAPE '!'" +
      "    ORDER BY SENT_TIMESTAMP ASC" +
      "    LIMIT ? OFFSET ?";
  //TODO: use UTF-8 encoding to compile
  // private final static String sql_text_insensitive =
  //   "    SELECT ID," +
  //     "    SENT_TIMESTAMP," +
  //     "    EDIT_TIMESTAMP," +
  //     "    MESSAGE_TYPE," +
  //     "    INDEX_STATUS," +
  //     "    SENDER," +
  //     "    DESTINATION," +
  //     "    TEXT, " +
  //     "    REACTIONS," +
  //     "    TYPE_EXTRAINFO" +
  //     "    FROM chat.MESSAGE " +
  //     "    WHERE SENDER = ? " +
  //     "    AND DESTINATION = ? " +
  //     "    AND TRANSLATE( LOWER(TEXT), 'áçéíóúàèìòùâêîôûãõëü', 'aceiouaeiouaeiouaoeu') LIKE TRANSLATE( ?, 'áçéíóúàèìòùâêîôûãõëü', 'aceiouaeiouaeiouaoeu') ESCAPE '!'" +
  //     "    ORDER BY SENT_TIMESTAMP ASC" +
  //     "    LIMIT ? OFFSET ?";

  private final static String sINSERT_MESSAGE_READ =
    "INSERT INTO chat.MESSAGE_READ (SENDER,DESTINATION,TIMESTAMP,MESSAGE_ID) VALUES (?,?,?,?) ";

  private final static String sUPDATE_MESSAGE_READ =
    "UPDATE chat.MESSAGE_READ SET MESSAGE_ID=?, TIMESTAMP=? WHERE SENDER=? AND DESTINATION=?";

  private final static String sSELECT_MESSAGE_READ =
    "SELECT TIMESTAMP,MESSAGE_ID FROM chat.MESSAGE_READ WHERE SENDER = ? AND DESTINATION = ?";

  private final static String sSELECT_ALL_MESSAGE_READ =
    "SELECT SENDER,DESTINATION,TIMESTAMP,MESSAGE_ID FROM chat.MESSAGE_READ WHERE DESTINATION = ?";

  private final static String sCOUNT_MESSAGE_TO_READ =
    "SELECT COUNT(*) FROM chat.MESSAGE WHERE SENDER = ? AND DESTINATION = ? AND (SENT_TIMESTAMP > ? OR EDIT_TIMESTAMP > ?) ";

  private final static String sCOUNT_MESSAGE_TO_READ_FROM_EVERYONE =
    "SELECT COUNT(*) FROM chat.MESSAGE WHERE DESTINATION = ? AND (SENT_TIMESTAMP > ? OR EDIT_TIMESTAMP > ?) ";

  private final static String sALL_RECIPIENTS =
      "SELECT DISTINCT chat.MESSAGE.SENDER " +
      " FROM chat.MESSAGE " +
      " WHERE chat.MESSAGE.DESTINATION = ? " +
      " UNION " +
      "SELECT chat.MESSAGE_READ.DESTINATION " +
      " FROM chat.MESSAGE_READ " +
      " WHERE chat.MESSAGE_READ.SENDER = ?";

  private final DbHelper          mChatDbHelper;
  private final SubdomainResolver mSubdomainResolver;

  @Inject
  public ImMessageStatements(
    ChatDbHandler chatDbHandler,
    SubdomainResolver subdomainResolver
  )
  {
    mChatDbHelper = new DbHelper(chatDbHandler);
    mSubdomainResolver = subdomainResolver;
  }

  public void insert(final ImMessage imMessage) throws SQLException
  {
    mChatDbHelper.executeTransactionQuery(sql_insert, new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement statement) throws SQLException
        {
          int i = 1;
          statement.setString(i++, imMessage.getId());
          statement.setLong(i++, imMessage.getSentTimestamp());
          statement.setLong(i++, imMessage.getEditTimestamp());
          statement.setShort(i++, EventType.toShort(imMessage.getEventType()));
          statement.setShort(i++, TargetType.toShort(imMessage.getTargetType()));
          statement.setShort(i++, imMessage.getIndexStatus());
          statement.setString(i++, imMessage.getText());
          statement.setString(i++, mSubdomainResolver.removeSubdomainFrom(imMessage.getTargetType(), imMessage.getSender()));
          statement.setString(i++, mSubdomainResolver.removeSubdomainFrom(imMessage.getTargetType(), imMessage.getDestination()));
          statement.setString(i++, imMessage.getReactions());
          statement.setString(i++, imMessage.getTypeExtrainfo());
          return i;
        }
      }
    );
  }

  public void update(final ImMessage imMessage) throws SQLException
  {
    mChatDbHelper.executeTransactionQuery(sql_update, new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement statement) throws SQLException
        {
          int i = 1;
          statement.setString(i++, imMessage.getId());
          statement.setLong(i++, imMessage.getSentTimestamp());
          statement.setLong(i++, imMessage.getEditTimestamp());
          statement.setShort(i++, EventType.toShort(imMessage.getEventType()));
          statement.setShort(i++, TargetType.toShort(imMessage.getTargetType()));
          statement.setShort(i++, imMessage.getIndexStatus());
          statement.setString(i++, imMessage.getText());
          statement.setString(i++, mSubdomainResolver.removeSubdomainFrom(imMessage.getTargetType(), imMessage.getSender()));
          statement.setString(i++, mSubdomainResolver.removeSubdomainFrom(imMessage.getTargetType(), imMessage.getDestination()));
          statement.setString(i++, imMessage.getReactions());
          statement.setString(i++, imMessage.getTypeExtrainfo());
          return i;
        }
      }
    );
  }

  public List<ImMessage> query(
    final String sender,
    final String destination,
    final Optional<Long> fromTime,
    final Optional<Long> toTime,
    final Optional<Integer> max) throws SQLException
  {
    final List<ImMessage> messages = new ArrayList<ImMessage>();
    List<String> where = new ArrayList<String>();

    StringBuilder sb = new StringBuilder(sSELECT_MESSAGE);
    if (!sender.isEmpty())
    {
      where.add(" SENDER = ?");
    }
    if (!destination.isEmpty())
    {
      where.add(" DESTINATION = ?");
    }
    if (fromTime.hasValue())
    {
      where.add(" (SENT_TIMESTAMP > ? OR (EDIT_TIMESTAMP <> 0 AND EDIT_TIMESTAMP > ?))");
    }
    if (toTime.hasValue())
    {
      where.add(" (SENT_TIMESTAMP < ? OR (EDIT_TIMESTAMP <> 0 AND EDIT_TIMESTAMP < ?))");
    }
    if (!where.isEmpty())
    {
      sb.append(" WHERE ");
      for (int i = 0; i<where.size(); i++)
      {
        String s = where.get(i);
        if (i != 0)
        {
          sb.append(" AND ");
        }
        sb.append(s);
      }
    }
    sb.append(sSELECT_MESSAGE_ORDER);

    mChatDbHelper.query(sb.toString(), new DbHelper.ParametersFactory()
    {
      @Override
      public int init(PreparedStatement preparedStatement) throws SQLException
      {
        int i = 1;
        if (!sender.isEmpty())
        {
          preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(sender));
        }
        if (!destination.isEmpty())
        {
          preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(destination));
        }
        if (fromTime.hasValue())
        {
          preparedStatement.setLong(i++, fromTime.getValue());
          preparedStatement.setLong(i++, fromTime.getValue());
        }
        if (toTime.hasValue())
        {
          preparedStatement.setLong(i++, toTime.getValue());
          preparedStatement.setLong(i++, toTime.getValue());
        }
        preparedStatement.setInt(i++, max.optValue(1000));
        return i;
      }
    }, new DbHelper.ResultSetFactory<Void>()
    {
      @Override
      public Void create(ResultSet rs) throws SQLException
      {
        int i = 1;
        messages.add(new ImMessage(
          rs.getString(i++),
          rs.getLong(i++),
          rs.getLong(i++),
          EventType.fromShort(rs.getShort(i++)),
          TargetType.fromShort(rs.getShort(i++)),
          rs.getShort(i++),
          mSubdomainResolver.toRoomAddress(rs.getString(i++)).resourceAddress(),
          mSubdomainResolver.toRoomAddress(rs.getString(i++)).resourceAddress(),
          rs.getString(i++),
          rs.getString(i++),
          rs.getString(i++)
        ));
        return null;
      }
    });

    return messages;
  }

  public void upsertMessageRead(final String sender,final String destination,final long timestamp,final String id) throws SQLException
  {
    DbHelper.DbConnection connection = mChatDbHelper.beginTransaction();
    try
    {
      Long currentTimestamp = getLastMessageRead(connection, null, sender, destination).getLeft();

      if (currentTimestamp == null)
      {
        mChatDbHelper.executeQuery(connection, sINSERT_MESSAGE_READ, new DbHelper.ParametersFactory()
        {
          @Override
          public int init(PreparedStatement preparedStatement) throws SQLException
          {
            int i = 1;
            preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(sender));
            preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(destination));
            preparedStatement.setLong(i++, timestamp);
            preparedStatement.setString(i++, id);
            return i;
          }
        });
      }
      else if (timestamp > currentTimestamp)
      {
        mChatDbHelper.executeQuery(connection, sUPDATE_MESSAGE_READ, new DbHelper.ParametersFactory()
        {
          @Override
          public int init(PreparedStatement preparedStatement) throws SQLException
          {
            int i = 1;
            preparedStatement.setString(i++, id);
            preparedStatement.setLong(i++, timestamp);
            preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(sender));
            preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(destination));
            return i;
          }
        });
      }
      connection.commitAndClose();
    }
    catch (SQLException e)
    {
      connection.rollbackAndClose();
      throw e;
    }
  }

  public Pair<Long,String> getLastMessageRead(final String sender, final String destination) throws SQLException
  {
    DbHelper.DbConnection connection = mChatDbHelper.beginTransaction();
    try
    {
      return getLastMessageRead(connection, 0L, sender, destination);
    }
    finally
    {
      connection.close();
    }
  }

  public Pair<Long,String> getLastMessageRead(DbHelper.DbConnection connection, Long defaultValue, final String sender, final String destination) throws SQLException
  {
    final Pair<Long,String> pair[] = new Pair[1];
    pair[0] = Pair.of(defaultValue,"");

    mChatDbHelper.executeQuery(
      connection,
      sSELECT_MESSAGE_READ,
      new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement preparedStatement) throws SQLException
        {
          int i = 1;
          preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(sender));
          preparedStatement.setString(i++, mSubdomainResolver.removeSubdomainFrom(destination));
          return i;
        }
      },
      new DbHelper.ResultSetFactory<Void>()
      {
        @Override
        public Void create(ResultSet rs) throws SQLException
        {
          long timestamp = rs.getLong(1);
          String id = rs.getString(2);
          pair[0] = Pair.of(timestamp,id);
          return null;
        }
      });

    return pair[0];
  }

  public ContainerListContainer getAllMessage(String user) throws SQLException
  {
    ContainerListContainer messages = new ContainerListContainer();
    List<ImMessage> list = query(user, "", Optional.<Long>empty(), Optional.<Long>empty(), Optional.<Integer>empty());
    for (ImMessage message : list)
    {
      Container container = new ContainerImpl();
      container.putString("ID",message.getId());
      container.putLong("SENT_TIMESTAMP",message.getSentTimestamp());
      container.putLong("EDIT_TIMESTAMP",message.getEditTimestamp());
      container.putLong("MESSAGE_TYPE",EventType.toShort(message.getEventType()));
      container.putLong("TARGET_TYPE",TargetType.toShort(message.getTargetType()));
      container.putLong("INDEX_STATUS",message.getIndexStatus());
      container.putString("TEXT",message.getText());
      container.putString("SENDER",mSubdomainResolver.removeSubdomainFrom(message.getTargetType(), message.getSender()));
      container.putString("DESTINATION",mSubdomainResolver.removeSubdomainFrom(message.getTargetType(), message.getDestination()));
      container.putString("REACTIONS",message.getReactions());
      container.putString("TYPE_EXTRAINFO",message.getTypeExtrainfo());
      messages.add(container);
    }

    return messages;
  }

  public ContainerListContainer getAllMessageRead(final String ackDestination) throws SQLException
  {
    final ContainerListContainer list = new ContainerListContainer();

    mChatDbHelper.query(sSELECT_ALL_MESSAGE_READ,
      new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, mSubdomainResolver.removeSubdomainFrom(ackDestination));
          return 2;
        }
      },
      new DbHelper.ResultSetFactory<Void>()
      {
        @Override
        public Void create(ResultSet rs) throws SQLException
        {
          ContainerImpl entry = new ContainerImpl();
          entry.putString("ACK_SENDER",rs.getString(1));
          entry.putString("ACK_DESTINATION",rs.getString(2));
          entry.putLong("TIMESTAMP",rs.getLong(3));
          entry.putString("MESSAGE_ID",rs.getString(4));
          list.add(entry);
          return null;
        }
      });
    return list;
  }

  public Set<String> getAllRecipients(final String destination) throws SQLException
  {
    final Set<String> set = new HashSet<String>();

    mChatDbHelper.query(sALL_RECIPIENTS,
      new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement preparedStatement) throws SQLException
        {
          String dst = mSubdomainResolver.removeSubdomainFrom(destination);
          preparedStatement.setString(1, dst);
          preparedStatement.setString(2, dst);
          return 3;
        }
      },
      new DbHelper.ResultSetFactory<Void>()
      {
        @Override
        public Void create(ResultSet rs) throws SQLException
        {
          set.add(mSubdomainResolver.toRoomAddress(rs.getString(1)).toString());
          return null;
        }
      });

    return set;
  }

  public Map<String,Pair<Integer,Long>> getCountMessageToRead(final String destination) throws SQLException
  {
    Map<String,Pair<Integer,Long>> map = new HashMap<String,Pair<Integer,Long>>();
    Set<String> recipients = getAllRecipients(destination);
    for (String sender : recipients)
    {
      Pair<Long, String> read = getLastMessageRead(destination, sender);
      long timestamp = read.getLeft();
      int count = getCountMessageToRead(sender, destination, timestamp);
      map.put(sender, Pair.<Integer, Long>of(count, timestamp));
    }
    return map;
  }

  public int getCountMessageToRead(final String sender,final String destination,final long timestamp) throws SQLException
  {
    final int[] count = new int[1];
    mChatDbHelper.query(sCOUNT_MESSAGE_TO_READ,
      new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, mSubdomainResolver.removeSubdomainFrom(sender));
          preparedStatement.setString(2, mSubdomainResolver.removeSubdomainFrom(destination));
          preparedStatement.setLong(3, timestamp);
          preparedStatement.setLong(4, timestamp);
          return 5;
        }
      },
      new DbHelper.ResultSetFactory<Void>()
      {
        @Override
        public Void create(ResultSet rs) throws SQLException
        {
          count[0] = rs.getInt(1);
          return null;
        }
      });

    return count[0];
  }

  public int getCountMessageToReadFromEveryone(final String destination,final long timestamp) throws SQLException
  {
    final int[] count = new int[1];
    mChatDbHelper.query(sCOUNT_MESSAGE_TO_READ_FROM_EVERYONE,
      new DbHelper.ParametersFactory()
      {
        @Override
        public int init(PreparedStatement preparedStatement) throws SQLException
        {
          preparedStatement.setString(1, mSubdomainResolver.removeSubdomainFrom(destination));
          preparedStatement.setLong(2, timestamp);
          preparedStatement.setLong(3, timestamp);
          return 4;
        }
      },
      new DbHelper.ResultSetFactory<Void>()
      {
        @Override
        public Void create(ResultSet rs) throws SQLException
        {
          count[0] = rs.getInt(1);
          return null;
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
      .replaceAll(Pattern.quote("!"), "!!")
      .replaceAll(Pattern.quote("%"), "!%")
      .replaceAll(Pattern.quote("_"), "!_")
      .replaceAll(Pattern.quote("["), "![");
  }
}
