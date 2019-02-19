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

package com.zextras.modules.chat.server.soap;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.zextras.lib.log.ChatLog;
//import com.zextras.modules.chat.ZxChatZimlet;
import com.zextras.lib.Error.ErrorCode;
import com.zextras.lib.Error.ZxError;
import com.zextras.lib.log.SeverityLevel;
import com.zextras.modules.chat.server.parsing.ParserFactory;
import com.zextras.modules.chat.server.soap.encoders.SoapEncoderFactory;
import org.openzal.zal.Account;
import org.openzal.zal.ContinuationThrowable;
import org.openzal.zal.lib.Filter;
import com.zextras.lib.filters.FilterPassAll;
import com.zextras.lib.json.JSONArray;
import com.zextras.lib.json.JSONObject;
import com.zextras.modules.chat.server.events.EventManager;
import com.zextras.modules.chat.server.exceptions.NoSuchAccountChatException;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionInPingException;
//import com.zextras.modules.chat.server.operations.CheckClientVersion;
import com.zextras.modules.chat.server.session.Session;
import com.zextras.modules.chat.server.session.SessionManager;
import com.zextras.modules.chat.server.session.SessionUUID;
import com.zextras.modules.chat.server.address.SpecificAddress;
import com.zextras.modules.chat.server.client_contstants.ClientEventType;
import com.zextras.modules.chat.server.events.Event;
import com.zextras.modules.chat.server.operations.ChatOperation;
import com.zextras.modules.chat.server.events.EventId;
import com.zextras.modules.chat.server.exceptions.NoSuchSessionException;
import com.zextras.modules.chat.server.soap.command.SoapCommand;
import com.zextras.modules.chat.server.exceptions.ParserException;
import com.zextras.modules.chat.server.parsing.Parser;
import org.openzal.zal.Utils;
import org.openzal.zal.soap.SoapResponse;
import org.openzal.zal.soap.ZimbraContext;

import java.util.List;

public class InitialSoapRequestHandler implements ChatSoapRequestHandler
{
  private final EventManager       mEventManager;
  private final SessionManager     mSessionManager;
  private final Account            mAccount;
  private final SoapEncoderFactory mSoapEncoderFactory;
  private final ZimbraContext      mZimbraContext;
  //private final ZxChatZimlet       mChatZimlet;
  private final ParserFactory      mSoapParserFactory;
  private final SoapResponse       mSoapResponse;

  @Inject
  public InitialSoapRequestHandler(
    EventManager eventManager,
    SessionManager sessionManager,
    SoapEncoderFactory soapEncoderFactory,
    ParserFactory soapParserFactory,
    @Assisted Account account,
    @Assisted ZimbraContext zimbraContext,
    //@Assisted ZxChatZimlet chatZimlet,
    @Assisted SoapResponse soapResponse
  )
  {
    mEventManager = eventManager;
    mSessionManager = sessionManager;
    mAccount = account;
    mSoapEncoderFactory = soapEncoderFactory;
    mZimbraContext = zimbraContext;
    //mChatZimlet = chatZimlet;
    mSoapParserFactory = soapParserFactory;
    mSoapResponse = soapResponse;
  }

  private Filter<Event> getCurrentOutFilters()
  {
    String sessionId = mZimbraContext.getParameter("session_id", "");
    if (!sessionId.isEmpty())
    {
      try
      {
        Session session = mSessionManager.getSessionById(SessionUUID.fromString(sessionId));
        return session.getOutFilter();
      }
      catch (NoSuchSessionException ex)
      {
        return new FilterPassAll<Event>();
      }
    }

    return new FilterPassAll<Event>();
  }

  @Override
  public void handleRequest()
  {
    // Check if the auth token comes from the same account of the session
    if (!mZimbraContext.getAuthenticatedAccontId().equals(mAccount.getId()))
    {
      sendShutdown();
      return;
    }

    final SoapCommand soapCommand;
    List<ChatOperation> operations;
    try
    {
      soapCommand = createCommand(mAccount.getName(), mZimbraContext);
      operations = soapCommand.createOperationList();
    }
    catch (NoSuchAccountChatException ex)
    {
      ChatLog.log.info(ex.getMessage());
      mSoapResponse.setValue("error", ex.toJSON().toString());
      return;
    }
    catch (Exception e)
    {
      ChatLog.log.crit(Utils.exceptionToString(e));
      ZxError newEx = new ZxError(SeverityLevel.ERROR, new ErrorCode()
    {
      public String getCodeString()
      {
        return "GENERIC_ERROR";
      }

      public String getMessage()
      {
        return "Generic Error: {details}";
      }
    });
      newEx.initCause(e);
      mSoapResponse.setValue("error", newEx.toJSON().toString());
      return;
    }

    if (!mZimbraContext.getParameter("session_id", "").isEmpty() )
    {
      SessionUUID session_id = SessionUUID.fromString(mZimbraContext.getParameter("session_id", ""));
      /* operations = new ArrayList<ChatOperation>(operations);
      operations.add(
        new CheckClientVersion(session_id,mChatZimlet)
      ); */
    }

    Filter<Event> outFilter = getCurrentOutFilters();
    try
    {
      mEventManager.execOperations(operations,outFilter);
    }
    catch (NoSuchSessionInPingException ex)
    {
      ChatLog.log.info("No session found for " + mAccount.getName() + ", requesting registration");
      sendRegistrationRequired();
      return;
    }
    catch (NoSuchSessionException ex)
    {
      ChatLog.log.info("ZeXtras Chat session appears to be expired, starting new session for " + mAccount.getName()); //#ZextrasRef
      ChatLog.log.crit("Exception: " + Utils.exceptionToString(ex));
      mSoapResponse.setValue("error", ex.toJSON().toString());
      return;
    }
    catch (ContinuationThrowable e)
    {
      throw e;
    }
    catch (Throwable e)
    {
      ChatLog.log.crit(Utils.exceptionToString(e));
      ZxError newEx = new ZxError(SeverityLevel.ERROR, new ErrorCode()
      {
        public String getCodeString()
        {
          return "GENERIC_ERROR";
        }

        public String getMessage()
        {
          return "Generic Error: {details}";
        }
      });
      newEx.initCause(e);
      mSoapResponse.setValue("error", newEx.toJSON().toString());
      return;
    }
  }

  private void sendRegistrationRequired()
  {
    JSONArray responseArray = new JSONArray();
    JSONObject response = new JSONObject();
    response.put("type", ClientEventType.REQUIRED_REGISTRATION);
    response.put("id", EventId.randomUUID());
    responseArray.add(response);
    mSoapResponse.setValue("responses", responseArray.toString());
  }

  private void sendShutdown()
  {
    ChatLog.log.info("Sending shutdown to " + mAccount.getName());
    JSONArray responseArray = new JSONArray();
    JSONObject response = new JSONObject();
    response.put("type", ClientEventType.TYPE_SHUTDOWN);
    response.put("id", EventId.randomUUID());
    responseArray.add(response);
    mSoapResponse.setValue("responses", responseArray.toString());
  }

  private SoapCommand createCommand(
    String accountAddress,
    ZimbraContext zimbraContext
  )
    throws ParserException
  {
    assert (accountAddress != null);
    final SpecificAddress senderAddress = new SpecificAddress(accountAddress, "soap");

    final Parser soapParser = mSoapParserFactory.create(
      senderAddress,
      zimbraContext,
      mSoapResponse,
      false
    );
    return soapParser.parse();
  }
}
