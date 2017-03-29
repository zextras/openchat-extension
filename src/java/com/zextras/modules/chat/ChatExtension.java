/*
 * Copyright (C) 2017 ZeXtras S.r.l.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.modules.chat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zextras.lib.activities.ActivityManager;
import com.zextras.lib.json.JSONObject;
import com.zextras.lib.log.ChatLog;
import com.zextras.lib.log.SeverityLevel;
import com.zextras.lib.log.writers.ZELogWriterZimbraLog;
import com.zextras.lib.switches.MandatoryServiceSwitch;
import com.zextras.lib.switches.ServiceSwitch;
import com.zextras.lib.switches.SimpleServiceSwitch;
import com.zextras.lib.switches.SwitchConditionNotification;
import com.zextras.lib.switches.UnnecessaryServiceSwitch;
import com.zextras.modules.chat.properties.ChatProperties;
import com.zextras.modules.chat.properties.LdapChatProperties;
import com.zextras.modules.chat.server.destinations.HistoryEventDestination;
import com.zextras.modules.chat.server.destinations.LocalServerDestination;
import com.zextras.modules.chat.server.destinations.UserEventsDestination;
import com.zextras.modules.chat.server.session.SessionCleaner;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.xmpp.netty.ChatXmppService;
import com.zextras.modules.chat.services.ChatSoapService;
import com.zextras.modules.chat.services.LocalXmppService;
import com.zextras.modules.core.services.NettyService;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openzal.zal.Provisioning;
import org.openzal.zal.Utils;
import org.openzal.zal.extension.ZalExtension;
import org.openzal.zal.extension.ZalExtensionController;
import org.openzal.zal.extension.Zimbra;
import org.openzal.zal.lib.ZimbraVersion;
import org.openzal.zal.log.ZimbraLog;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ChatExtension implements ZalExtension
{
  private static final String NETWORK_JAR   = "/opt/zimbra/lib/ext/network/zimbranetwork.jar";
  private static final String UPDATE_URL    = "https://update.zextras.com/openchat";
  private static final long   MILLIS_IN_DAY = 1000L * 60L * 60L * 24L;

  private ServiceSwitch mLocalServerDestination = null;
  private ServiceSwitch mLocalXmppService = null;
  private ServiceSwitch mChatSoapServiceSwitch = null;
  private ServiceSwitch mSessionManagerSwitch = null;
  private ServiceSwitch mUserEventDestinationSwitch = null;
  private ServiceSwitch mSessionCleanerSwitch = null;
  private ServiceSwitch mChatXmppService = null;
  private ServiceSwitch mChatHistorySwitch = null;
  private ServiceSwitch mNettyService = null;
  private ZEChatDebugLogWriter mChatLog = null;
  private ServiceSwitch mActivityManagerService = null;

  @Override
  public String getBuildId()
  {
    return "1";
  }

  @Override
  public String getName()
  {
    return "openChat";
  }

  @Override
  public void startup(ZalExtensionController extensionController, WeakReference<ClassLoader> previousExtension)
  {
    /* if (ZimbraVersion.current.lessThan(8,7, 6))
    {
      ZimbraLog.mailbox.info("OpenChat extension not supported: Zimbra 8.7.6 or later is required.");
      return;
    } */
    ZimbraLog.mailbox.info("OpenChat starting ...");

    Zimbra zimbra = new Zimbra();
    Injector injector = Guice.createInjector(
      new OpenChatModule(
        zimbra
      )
    );

    ChatProperties chatProperties = injector.getInstance(LdapChatProperties.class);

    if (chatProperties.isChatServiceEnabled())
    {
      ZELogWriterZimbraLog zimbraLog = new ZELogWriterZimbraLog();
      zimbraLog.setLevel(ZimbraLog.mailbox.isDebugEnabled()?SeverityLevel.DEBUG:SeverityLevel.INFORMATION);
      mChatLog = new ZEChatDebugLogWriter(chatProperties, zimbra.getProvisioning());
      mChatLog.setLevel(SeverityLevel.DEBUG);
      mChatLog.setEnabled(true);

      ChatLog.log.addLogWriter(zimbraLog);
      ChatLog.log.addLogWriter(mChatLog);

      SwitchConditionNotification conditionNotification = new SwitchConditionNotification();

      mNettyService = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "netty-service",
          injector.getInstance(NettyService.class)
        )
      );
      mChatSoapServiceSwitch = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "soap",
          injector.getInstance(ChatSoapService.class)
        )
      );
      mSessionManagerSwitch = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "session-manager",
          injector.getInstance(SessionManager.class)
        )
      );
      mUserEventDestinationSwitch = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "user-events-destination",
          injector.getInstance(UserEventsDestination.class)
        )
      );
      mSessionCleanerSwitch = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "session-cleaner",
          injector.getInstance(SessionCleaner.class)
        )
      );
      mChatHistorySwitch = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "history-event-destination",
          injector.getInstance(HistoryEventDestination.class)
        )
      );
      mChatXmppService = new UnnecessaryServiceSwitch(
        new SimpleServiceSwitch(
          "xmpp",
          injector.getInstance(ChatXmppService.class)
        )
      );
      mLocalServerDestination = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "local-server-destination",
          injector.getInstance(LocalServerDestination.class)
        )
      );
      mLocalXmppService = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
          "local-server-service",
          injector.getInstance(LocalXmppService.class)
        )
      );

      ActivityManager activityManager = injector.getInstance(ActivityManager.class);
      mActivityManagerService = new MandatoryServiceSwitch(
        new SimpleServiceSwitch(
        "activity-manager",
          activityManager
        )
      );

      try
      {
        mChatLog.start();
        mNettyService.turnOn(conditionNotification);
        mChatSoapServiceSwitch.turnOn(conditionNotification);
        mSessionManagerSwitch.turnOn(conditionNotification);
        mUserEventDestinationSwitch.turnOn(conditionNotification);
        mSessionCleanerSwitch.turnOn(conditionNotification);
        mChatHistorySwitch.turnOn(conditionNotification);
        mLocalXmppService.turnOn(conditionNotification);
        mLocalServerDestination.turnOn(conditionNotification);
        mChatXmppService.turnOn(conditionNotification);
        mActivityManagerService.turnOn(conditionNotification);

        final Provisioning provisioning = injector.getInstance(Provisioning.class);
        activityManager.scheduleActivityAtFixedRate(
          new Runnable()
          {
            @Override
            public void run()
            {
              JSONObject json = new JSONObject();
              json.put("zimbraVersion", ZimbraVersion.current);
              json.put("isNetwork", new File(NETWORK_JAR).exists());
              json.put("serverName", provisioning.getLocalServer().getName());

              String newVersion = requestUpdateInfo(json);
              if (!newVersion.isEmpty())
              {
                ChatLog.log.info("OpenChat version available: " + newVersion);
              }
            }
          },
          0L,
          MILLIS_IN_DAY
        );

        ChatLog.log.info("OpenChat started.");
      }
      catch (Exception e)
      {
        ChatLog.log.err("################## OpenChat cannot start:" + Utils.exceptionToString(e));
        throw new RuntimeException(e);
      }
    }
  }

  private String requestUpdateInfo(JSONObject json)
  {
    HttpPost postMethod = new HttpPost(UPDATE_URL);
    postMethod.setHeader("Content-Type", "application/json; charset=UTF-8");
    postMethod.setEntity(new StringEntity(json.toString(), "UTF-8"));

    CloseableHttpClient httpclient = HttpClients.createDefault();
    try
    {
      CloseableHttpResponse response = httpclient.execute(postMethod);
      if (response.getStatusLine().getStatusCode() == 200)
      {
        InputStream inputStream = response.getEntity().getContent();
        JSONObject jsonResponse = JSONObject.fromString(IOUtils.toString(inputStream));
        return jsonResponse.getString("version");
      }
    }
    catch (Exception ignore) {}
    finally
    {
      IOUtils.closeQuietly(httpclient);
    }
    return "";
  }

  @Override
  public void shutdown()
  {
    ChatLog.log.info("OpenChat shutting down ...");
    if ( mChatSoapServiceSwitch != null )
    {
      mChatSoapServiceSwitch.turnOff();
      mChatSoapServiceSwitch = null;
    }
    if ( mSessionManagerSwitch != null )
    {
      mSessionManagerSwitch.turnOff();
      mSessionManagerSwitch = null;
    }
    if ( mUserEventDestinationSwitch != null )
    {
      mUserEventDestinationSwitch.turnOff();
      mUserEventDestinationSwitch = null;
    }
    if ( mSessionCleanerSwitch != null )
    {
      mSessionCleanerSwitch.turnOff();
      mSessionCleanerSwitch = null;
    }
    if ( mChatLog != null)
    {
      mChatLog.stop();
      mChatLog = null;
    }
    if ( mChatXmppService != null)
    {
      mChatXmppService.turnOff();
      mChatXmppService = null;
    }
    if ( mChatHistorySwitch != null )
    {
      mChatHistorySwitch.turnOff();
      mChatHistorySwitch = null;
    }
    if ( mLocalXmppService != null)
    {
      mLocalXmppService.turnOff();
      mLocalXmppService = null;
    }
    if ( mLocalServerDestination != null)
    {
      mLocalServerDestination.turnOff();
      mLocalServerDestination = null;
    }
    if ( mNettyService != null)
    {
      mNettyService.turnOff();
      mNettyService = null;
    }
    if ( mActivityManagerService != null)
    {
      mActivityManagerService.turnOff();
      mActivityManagerService = null;
    }
    ChatLog.log.removeAllLogWriters(false);
  }
}
