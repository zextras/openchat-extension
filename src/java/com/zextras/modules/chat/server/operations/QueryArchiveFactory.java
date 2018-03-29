package com.zextras.modules.chat.server.operations;

import com.zextras.lib.Optional;
import com.google.inject.assistedinject.Assisted;
import com.zextras.modules.chat.server.address.SpecificAddress;

public interface QueryArchiveFactory
{
  QueryArchive create(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("queryId") String queryId,
    @Assisted("with") Optional<String> with,
    @Assisted("start") Optional<Long> start,
    @Assisted("end") Optional<Long> end,
    @Assisted("node") Optional<String> node,
    @Assisted("max") Optional<Integer> max
  );
}
