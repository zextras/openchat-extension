package com.zextras.modules.chat.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.ZimbraSSLContextProvider;
import com.zextras.lib.log.ChatLog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Testing : https://github.com/drwetter/testssl.sh
 *
 * ./testssl.sh --ssl-native -n -t xmpp --xmpphost example.com 192.168.0.2:5222
 * ./testssl.sh --ssl-native -n --xmpphost example.com 192.168.0.2:5223
 * ./testssl.sh --ssl-native -n --xmpphost example.com 192.168.0.2:5269
 */
@Singleton
public class SSLCipher
{
  private final ZimbraSSLContextProvider mZimbraSSLContextProvider;
  private AtomicBoolean mFirstTime;

  @Inject
  public SSLCipher(ZimbraSSLContextProvider zimbraSSLContextProvider)
  {
    mZimbraSSLContextProvider = zimbraSSLContextProvider;
    mFirstTime = new AtomicBoolean(true);
  }

  public void setCiphers(SSLContext sslContext,SSLEngine sslEngine)
  {
    String[] protocols = mZimbraSSLContextProvider.getMailboxdSslProtocols();
    if (protocols.length > 0)
    {
      sslEngine.setEnabledProtocols(protocols);
    }
    sslEngine.setEnabledCipherSuites(mZimbraSSLContextProvider.getSslCiphers(sslContext));
    SSLParameters params = sslEngine.getSSLParameters();
    if (params == null)
    {
      params = new SSLParameters();
    }
    try
    {
      params.setUseCipherSuitesOrder(true);
      sslEngine.setSSLParameters(params);
    }
    catch (java.lang.NoSuchMethodError e)
    {
      if (mFirstTime.get())
      {
        ChatLog.log.warn("Cipher Suites Order is supported from java 1.8");
        mFirstTime.set(false);
      }
    }
  }
}
