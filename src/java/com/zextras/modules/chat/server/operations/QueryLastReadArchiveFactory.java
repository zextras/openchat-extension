package com.zextras.modules.chat.server.operations;

import com.google.common.base.Optional;
import com.google.inject.assistedinject.Assisted;
import com.zextras.modules.chat.server.address.SpecificAddress;

public interface QueryLastReadArchiveFactory
{
  QueryLastReadArchive create(
    @Assisted("senderAddress") SpecificAddress senderAddress,
    @Assisted("with") Optional<String> with,
    @Assisted("start") Optional<Long> start,
    @Assisted("end") Optional<Long> end,
    @Assisted("node") Optional<String> node,
    @Assisted("max") Optional<Integer> max
  );
}
