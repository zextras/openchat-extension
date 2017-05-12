package com.zextras.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class OpenChat
{
  public static void main(String[] args)
  {
    try
    {
      StringBuilder infoBuilder = new StringBuilder();
      infoBuilder.append("OpenChat Zimbra Extension")
        .append("\n")
        .append(" Version: ").append(BuildInfo.Version)
        .append("\n")
        .append(" Commit: ").append(BuildInfo.COMMIT)
        .append("\n")
        .append("OpenChat Zimlet")
        .append("\n");

      File zimletVersionFile = new File("/opt/zimbra/zimlets-deployed/com_zextras_chat_open/VERSION");
      if (zimletVersionFile.exists() && zimletVersionFile.canRead())
      {
        BufferedReader reader = new BufferedReader(new FileReader(zimletVersionFile));
        infoBuilder.append(" Version: ").append(reader.readLine())
          .append("\n")
          .append(" Commit: ").append(reader.readLine());
      }
      else
      {
        infoBuilder.append(" Not available.");
      }


      System.out.println(infoBuilder.toString());
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      System.exit(1);
    }

    System.exit(0);
  }
}
