package com.zextras.modules.chat.server.db.sql;

import com.zextras.lib.sql.ResultSetParser;
import com.zextras.modules.chat.server.ImMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageFactory implements ResultSetParser<ImMessage>
{
  @Override
  public ImMessage readFromResultSet(ResultSet rs) throws SQLException
  {
    int i = 1;
    return new ImMessage(
      rs.getString(i++),
      rs.getLong(i++),
      rs.getLong(i++),
      rs.getShort(i++),
      rs.getBoolean(i++),
      rs.getShort(i++),
      rs.getString(i++),
      rs.getString(i++),
      rs.getString(i++),
      rs.getString(i++),
      rs.getString(i++)
    );
  }
}
