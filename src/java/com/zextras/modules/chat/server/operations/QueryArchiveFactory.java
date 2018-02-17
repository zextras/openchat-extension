package com.zextras.modules.chat.server.operations;

import com.google.inject.assistedinject.Assisted;
import com.zextras.modules.chat.server.address.SpecificAddress;

public interface QueryArchiveFactory
{
  QueryArchive create(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("queryid") String queryid,
    @Assisted("with") String with,
    @Assisted("start") String start,
    @Assisted("end") String end,
    @Assisted("node") String node,
    @Assisted("max") long max
  );
}
