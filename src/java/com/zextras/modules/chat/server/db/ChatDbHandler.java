package com.zextras.modules.chat.server.db;

import com.zextras.lib.db.DbHandler;
import com.zextras.modules.chat.server.db.providers.DbInfo;
import com.zextras.modules.chat.server.exceptions.ChatDbException;

public interface ChatDbHandler extends DbHandler
{
  DbInfo getDatabaseInfo() throws ChatDbException;
}
