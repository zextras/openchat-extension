package com.zextras.modules.chat.server;

import com.google.inject.Inject;
import com.zextras.lib.ZimbraSSLContextProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class SSLCipher
{
  private final ZimbraSSLContextProvider mZimbraSSLContextProvider;

  @Inject
  public SSLCipher(ZimbraSSLContextProvider zimbraSSLContextProvider)
  {
    mZimbraSSLContextProvider = zimbraSSLContextProvider;
  }

  private void setCiphers(SSLContext sslContext,SSLEngine sslEngine)
  {
    sslEngine.setEnabledProtocols(mZimbraSSLContextProvider.getMailboxdSslProtocols());
    sslEngine.setEnabledCipherSuites(mZimbraSSLContextProvider.getSslCiphers(sslContext));
    SSLParameters params = sslEngine.getSSLParameters();
    params.setUseCipherSuitesOrder(true);
    sslEngine.setSSLParameters(params);
  }
}
