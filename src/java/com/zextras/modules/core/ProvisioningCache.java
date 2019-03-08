package com.zextras.modules.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zextras.lib.FixedCacheMap;
import com.zextras.lib.FixedCacheStringTTLMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openzal.zal.*;
import org.openzal.zal.exceptions.NoSuchAccountException;
import org.openzal.zal.exceptions.NoSuchGrantException;
import org.openzal.zal.exceptions.UnableToFindDistributionListException;
import org.openzal.zal.exceptions.ZimbraException;
import org.openzal.zal.lib.Clock;
import org.openzal.zal.lib.Filter;
import org.openzal.zal.provisioning.TargetType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public class ProvisioningCache implements Provisioning
{
  private final static int                             sSIZE = 65536;
  private final static long                            sTTL  = 2L * 60L * 1000L;
  private final        Provisioning                    mProvisioning;
  private final        Clock                           mClock;
  private final        FixedCacheStringTTLMap<Account> mFixedCacheStringTTLMap;

  @Inject
  public ProvisioningCache(
    Provisioning provisioning,
    Clock clock
  )
  {
    mProvisioning = provisioning;
    mClock = clock;
    mFixedCacheStringTTLMap = new FixedCacheStringTTLMap<>(
      sSIZE,
      new FixedCacheMap.Getter<String, Account>() {
        @Override
        public Account get(String key)
        {
          return mProvisioning.getAccountByName(key);
        }
      },
      mClock,
      sTTL,
      true
    );
  }
  
  @Override
  @Nullable
  public Account getAccountByName(String accountStr)
    throws NoSuchAccountException
  {
    return mFixedCacheStringTTLMap.get(accountStr);
  }

  @Override
  @NotNull
  public Account assertAccountByName(String accountStr)
    throws NoSuchAccountException
  {
    Account account = mFixedCacheStringTTLMap.get(accountStr);
    if (account == null)
    {
      throw new NoSuchAccountException(accountStr);
    }
    return account;
  }

  @Override
  public boolean isValidUid(
    @NotNull
      String uid
  )
  {
    return mProvisioning.isValidUid(uid);
  }

  @Override
  @NotNull
  public Account getZimbraUser()
    throws ZimbraException
  {
    return mProvisioning.getZimbraUser();
  }

  @Override
  public OperationContext createZContext()
  {
    return mProvisioning.createZContext();
  }

  @Override
  @Nullable
  public DistributionList getDistributionListById(String id)
    throws ZimbraException
  {
    return mProvisioning.getDistributionListById(id);
  }

  @Override
  @Nullable
  public DistributionList getDistributionListByName(String name)
    throws ZimbraException
  {
    return mProvisioning.getDistributionListByName(name);
  }

  @Override
  public void visitAllAccounts(
    @NotNull
      SimpleVisitor<Account> visitor
  )
    throws ZimbraException
  {
    mProvisioning.visitAllAccounts(visitor);
  }

  @Override
  public void visitAllLocalAccountsNoDefaults(
    @NotNull
      SimpleVisitor<Account> visitor
  )
    throws ZimbraException
  {
    mProvisioning.visitAllLocalAccountsNoDefaults(visitor);
  }

  @Override
  public void visitAllAccounts(
    @NotNull
      SimpleVisitor<Account> visitor,
    @NotNull
      Filter<Account> filterAccounts
  )
    throws ZimbraException
  {
    mProvisioning.visitAllAccounts(visitor, filterAccounts);
  }

  @Override
  public void visitAllLocalAccountsSlow(
    @NotNull
      SimpleVisitor<Account> visitor,
    @NotNull
      Filter<Account> filterAccounts
  )
    throws ZimbraException
  {
    mProvisioning.visitAllLocalAccountsSlow(visitor, filterAccounts);
  }

  @Override
  public void visitAccountByIdNoDefaults(SimpleVisitor<Account> visitor, ZimbraId accountId)
  {
    mProvisioning.visitAccountByIdNoDefaults(visitor, accountId);
  }

  @Override
  public void visitAllDomains(
    @NotNull
      SimpleVisitor<Domain> visitor
  )
    throws ZimbraException
  {
    mProvisioning.visitAllDomains(visitor);
  }

  @Override
  public void visitDomain(
    @NotNull
      SimpleVisitor<Account> visitor,
    @NotNull
      Domain domain
  )
    throws ZimbraException
  {
    mProvisioning.visitDomain(visitor, domain);
  }

  @Override
  public Collection<String> getGroupMembers(String list)
    throws UnableToFindDistributionListException
  {
    return mProvisioning.getGroupMembers(list);
  }

  @Override
  public void authAccount(
    @NotNull
      Account account,
    String password,
    @NotNull
      Protocol protocol,
    Map<String, Object> context
  )
    throws ZimbraException
  {
    mProvisioning.authAccount(account, password, protocol, context);
  }

  @Override
  public Account getAccountByAccountIdOrItemId(String id)
  {
    return mProvisioning.getAccountByAccountIdOrItemId(id);
  }

  @Override
  @Nullable
  public Account getAccountById(String accountId)
    throws ZimbraException
  {
    return mProvisioning.getAccountById(accountId);
  }

  @Override
  @NotNull
  public Server getLocalServer()
    throws ZimbraException
  {
    return mProvisioning.getLocalServer();
  }

  @Override
  @Nullable
  public Domain getDomainByName(String domainName)
    throws ZimbraException
  {
    return mProvisioning.getDomainByName(domainName);
  }

  @Override
  public List<Domain> getAllDomains()
    throws ZimbraException
  {
    return mProvisioning.getAllDomains();
  }

  @Override
  @NotNull
  public Zimlet getZimlet(String zimletName)
    throws ZimbraException
  {
    return mProvisioning.getZimlet(zimletName);
  }

  @Override
  public void modifyAttrs(
    @NotNull
      Entry entry,
    Map<String, Object> attrs
  )
    throws ZimbraException
  {
    mProvisioning.modifyAttrs(entry, attrs);
  }

  @Override
  @Nullable
  public Domain getDomainById(String domainId)
    throws ZimbraException
  {
    return mProvisioning.getDomainById(domainId);
  }

  @Override
  public List<DistributionList> getAllDistributionLists(
    @NotNull
      Domain domain
  )
    throws ZimbraException
  {
    return mProvisioning.getAllDistributionLists(domain);
  }

  @Override
  public List<Group> getAllGroups(Domain domain)
    throws ZimbraException
  {
    return mProvisioning.getAllGroups(domain);
  }

  @Override
  @Nullable
  public Cos getCosById(String cosId)
    throws ZimbraException
  {
    return mProvisioning.getCosById(cosId);
  }

  @Override
  public List<Cos> getAllCos()
    throws ZimbraException
  {
    return mProvisioning.getAllCos();
  }

  @Override
  @Nullable
  public Cos getCosByName(String cosStr)
    throws ZimbraException
  {
    return mProvisioning.getCosByName(cosStr);
  }

  @Override
  @Nullable
  public DistributionList get(
    @NotNull
      ProvisioningKey.ByDistributionList id,
    String dlStr
  )
    throws ZimbraException
  {
    return mProvisioning.get(id, dlStr);
  }

  @Override
  @Nullable
  public Account get(
    @NotNull
      ProvisioningKey.ByAccount by,
    String target
  )
    throws ZimbraException
  {
    return mProvisioning.get(by, target);
  }

  @Override
  @NotNull
  public Account assertAccountById(String accountStr)
    throws NoSuchAccountException
  {
    return mProvisioning.assertAccountById(accountStr);
  }


  @Override
  public List<Account> getAllAdminAccounts()
    throws ZimbraException
  {
    return mProvisioning.getAllAdminAccounts();
  }

  @Override
  public List<Account> getAllAccounts(
    @NotNull
      Domain domain
  )
    throws ZimbraException
  {
    return mProvisioning.getAllAccounts(domain);
  }

  @Override
  public List<Server> getAllServers()
    throws ZimbraException
  {
    return mProvisioning.getAllServers();
  }

  @Override
  public List<Server> getAllServers(String service)
    throws ZimbraException
  {
    return mProvisioning.getAllServers(service);
  }

  @Override
  public List<CalendarResource> getAllCalendarResources(
    @NotNull
      Domain domain
  )
    throws ZimbraException
  {
    return mProvisioning.getAllCalendarResources(domain);
  }

  @Override
  public List<Zimlet> listAllZimlets()
    throws ZimbraException
  {
    return mProvisioning.listAllZimlets();
  }

  @Override
  public List<XMPPComponent> getAllXMPPComponents()
    throws ZimbraException
  {
    return mProvisioning.getAllXMPPComponents();
  }

  @Override
  @Nullable
  public GlobalGrant getGlobalGrant()
    throws ZimbraException
  {
    return mProvisioning.getGlobalGrant();
  }

  @Override
  @NotNull
  public Config getConfig()
    throws ZimbraException
  {
    return mProvisioning.getConfig();
  }

  @Override
  public List<UCService> getAllUCServices()
    throws ZimbraException
  {
    return mProvisioning.getAllUCServices();
  }

  @Override
  @Nullable
  public CalendarResource getCalendarResourceByName(String resourceName)
    throws ZimbraException
  {
    return mProvisioning.getCalendarResourceByName(resourceName);
  }

  @Override
  @Nullable
  public CalendarResource getCalendarResourceById(String resourceId)
    throws ZimbraException
  {
    return mProvisioning.getCalendarResourceById(resourceId);
  }

  @Override
  @Nullable
  public Domain createDomain(String currentDomainName, Map<String, Object> stringObjectMap)
    throws ZimbraException
  {
    return mProvisioning.createDomain(currentDomainName, stringObjectMap);
  }

  @Override
  @Nullable
  public Cos createCos(String cosname, Map<String, Object> stringObjectMap)
    throws ZimbraException
  {
    return mProvisioning.createCos(cosname, stringObjectMap);
  }

  @Override
  @Nullable
  public DistributionList createDistributionList(String dlistName)
    throws ZimbraException
  {
    return mProvisioning.createDistributionList(dlistName);
  }

  @Override
  @Nullable
  public DistributionList createDistributionList(
    String dlistName,
    Map<String, Object> stringObjectMap
  )
    throws ZimbraException
  {
    return mProvisioning.createDistributionList(dlistName, stringObjectMap);
  }

  @Override
  @Nullable
  public Group createDynamicGroup(String groupName)
    throws ZimbraException
  {
    return mProvisioning.createDynamicGroup(groupName);
  }

  @Override
  @Nullable
  public Group createDynamicGroup(String groupName, Map<String, Object> stringObjectMap)
    throws ZimbraException
  {
    return mProvisioning.createDynamicGroup(groupName, stringObjectMap);
  }

  @Override
  @Nullable
  public Account createCalendarResource(
    String dstAccount,
    String newPassword,
    Map<String, Object> attrs
  )
    throws ZimbraException
  {
    return mProvisioning.createCalendarResource(dstAccount, newPassword, attrs);
  }

  @Override
  @Nullable
  public Account createAccount(
    String dstAccount,
    @Nullable
      String newPassword,
    Map<String, Object> attrs
  )
    throws ZimbraException
  {
    return mProvisioning.createAccount(dstAccount, newPassword, attrs);
  }

  @Override
  public void restoreAccount(String emailAddress, Map<String, Object> attrs)
  {
    mProvisioning.restoreAccount(emailAddress, attrs);
  }

  @Override
  public DataSource restoreDataSource(Account account, DataSourceType dsType, String dsName, Map<String, Object> dataSourceAttrs)
  {
    return mProvisioning.restoreDataSource(account, dsType, dsName, dataSourceAttrs);
  }

  @Override
  public Identity restoreIdentity(Account account, String identityName, Map<String, Object> identityAttrs)
  {
    return mProvisioning.restoreIdentity(account, identityName, identityAttrs);
  }

  @Override
  public Signature restoreSignature(Account account, String signatureName, Map<String, Object> signatureAttrs)
  {
    return mProvisioning.restoreSignature(account, signatureName, signatureAttrs);
  }

  @Override
  public void restoreCos(Map<String, Object> attributes)
  {
    mProvisioning.restoreCos(attributes);
  }

  @Override
  public void restoreDomain(Map<String, Object> attributes)
  {
    mProvisioning.restoreDomain(attributes);
  }

  @Override
  public void restoreDistributionList(String name, Map<String, Object> attributes)
  {
    mProvisioning.restoreDistributionList(name, attributes);
  }

  @Override
  @Nullable
  public Server createServer(String name, Map<String, Object> attrs)
    throws ZimbraException
  {
    return mProvisioning.createServer(name, attrs);
  }

  @Override
  public void modifyIdentity(
    @NotNull
      Account newAccount,
    String identityName,
    Map<String, Object> newAttrs
  )
    throws ZimbraException
  {
    mProvisioning.modifyIdentity(newAccount, identityName, newAttrs);
  }

  @Override
  public void grantRight(
    String targetType,
    @NotNull
      Targetby targetBy,
    String target,
    String granteeType,
    @NotNull
      GrantedBy granteeBy,
    String grantee,
    String right
  )
    throws ZimbraException
  {
    mProvisioning.grantRight(targetType, targetBy, target, granteeType, granteeBy, grantee, right);
  }

  @Override
  public void revokeRight(
    String targetType,
    Targetby targetBy,
    String target,
    String granteeType,
    @NotNull
      GrantedBy granteeBy,
    String grantee,
    String right
  )
    throws NoSuchGrantException
  {
    mProvisioning.revokeRight(targetType, targetBy, target, granteeType, granteeBy, grantee, right);
  }

  @Override
  public void revokeRight(
    String targetType,
    Targetby targetBy,
    String target,
    String granteeType,
    @NotNull
      GrantedBy granteeBy,
    String grantee,
    String right,
    RightModifier rightModifier
  )
    throws NoSuchGrantException
  {
    mProvisioning.revokeRight(targetType, targetBy, target, granteeType, granteeBy, grantee, right, rightModifier);
  }

  @Override
  public boolean checkRight(
    String targetType,
    Targetby targetBy,
    String target,
    GrantedBy granteeBy,
    String granteeVal,
    String right
  )
  {
    return mProvisioning.checkRight(targetType, targetBy, target, granteeBy, granteeVal, right);
  }

  @Override
  @Nullable
  public Grants getGrants(
    String targetType,
    Targetby targetBy,
    String target,
    String granteeType,
    GrantedBy granteeBy,
    String grantee,
    boolean granteeIncludeGroupsGranteeBelongs
  )
  {
    return mProvisioning.getGrants(
      targetType,
      targetBy,
      target,
      granteeType,
      granteeBy,
      grantee,
      granteeIncludeGroupsGranteeBelongs
    );
  }

  @Override
  public <T> T toZimbra(
    @NotNull
      Class<T> cls
  )
  {
    return mProvisioning.toZimbra(cls);
  }

  @Override
  @Nullable
  public Domain getDomain(
    @NotNull
      Account account
  )
    throws ZimbraException
  {
    return mProvisioning.getDomain(account);
  }

  @Override
  public void flushCache(
    @NotNull
      CacheEntryType cacheEntryType,
    @Nullable
      Collection<CacheEntry> cacheEntries
  )
    throws ZimbraException
  {
    mProvisioning.flushCache(cacheEntryType, cacheEntries);
  }

  @Override
  public CountAccountResult countAccount(
    @NotNull
      Domain domain
  )
    throws ZimbraException
  {
    return mProvisioning.countAccount(domain);
  }

  @Override
  public long getAccountsOnCos(
    @NotNull
      Domain domain,
    @NotNull
      Cos cos
  )
  {
    return mProvisioning.getAccountsOnCos(domain, cos);
  }

  @Override
  public long getMaxAccountsOnCos(
    @NotNull
      Domain domain,
    @NotNull
      Cos cos
  )
  {
    return mProvisioning.getMaxAccountsOnCos(domain, cos);
  }

  @Override
  @Nullable
  public Server getServer(
    @NotNull
      Account acct
  )
    throws ZimbraException
  {
    return mProvisioning.getServer(acct);
  }

  @Override
  @Nullable
  public Server getServerById(String id)
    throws ZimbraException
  {
    return mProvisioning.getServerById(id);
  }

  @Override
  @Nullable
  public Server getServerByName(String name)
    throws ZimbraException
  {
    return mProvisioning.getServerByName(name);
  }

  @Override
  public boolean onLocalServer(
    @NotNull
      Account userAccount
  )
    throws ZimbraException
  {
    return mProvisioning.onLocalServer(userAccount);
  }

  @Override
  @Nullable
  public Zimlet createZimlet(String name, Map<String, Object> attrs)
    throws ZimbraException
  {
    return mProvisioning.createZimlet(name, attrs);
  }

  @Override
  public long getEffectiveQuota(
    @NotNull
      Account account
  )
  {
    return mProvisioning.getEffectiveQuota(account);
  }

  @Override
  public void setZimletPriority(String zimletName, int priority)
  {
    mProvisioning.setZimletPriority(zimletName, priority);
  }

  @Override
  public List<Account> getAllDelegatedAdminAccounts()
    throws ZimbraException
  {
    return mProvisioning.getAllDelegatedAdminAccounts();
  }

  @Override
  @Nullable
  public Group getGroupById(String dlStr)
    throws ZimbraException
  {
    return mProvisioning.getGroupById(dlStr);
  }

  @Override
  @Nullable
  public Group getGroupByName(String dlStr)
    throws ZimbraException
  {
    return mProvisioning.getGroupByName(dlStr);
  }

  @Override
  public void removeGranteeId(String target, String grantee_id, String granteeType, String right)
    throws ZimbraException
  {
    mProvisioning.removeGranteeId(target, grantee_id, granteeType, right);
  }

  @Override
  @Nullable
  public Grants getGrants(
    @NotNull
      TargetType targetType,
    Targetby name,
    String targetName,
    boolean granteeIncludeGroupsGranteeBelongs
  )
  {
    return mProvisioning.getGrants(targetType, name, targetName, granteeIncludeGroupsGranteeBelongs);
  }

  @Override
  public String getGranteeName(
    String grantee_id,
    @NotNull
      String grantee_type
  )
    throws ZimbraException
  {
    return mProvisioning.getGranteeName(grantee_id, grantee_type);
  }

  @Override
  @NotNull
  public GalSearchResult galSearch(
    @NotNull
      Account account,
    String query,
    int skip,
    int limit
  )
  {
    return mProvisioning.galSearch(account, query, skip, limit);
  }

  @Override
  @NotNull
  public Domain assertDomainById(String domainId)
  {
    return mProvisioning.assertDomainById(domainId);
  }

  @Override
  @NotNull
  public Domain assertDomainByName(String domainId)
  {
    return mProvisioning.assertDomainByName(domainId);
  }

  @Override
  @NotNull
  public Zimlet assertZimlet(String com_zextras_zextras)
  {
    return mProvisioning.assertZimlet(com_zextras_zextras);
  }

  @Override
  @NotNull
  public DistributionList assertDistributionListById(String targetId)
  {
    return mProvisioning.assertDistributionListById(targetId);
  }

  @Override
  public void deleteAccountByName(String id)
  {
    mProvisioning.deleteAccountByName(id);
  }

  @Override
  @NotNull
  public void deleteAccountById(String id)
  {
    mProvisioning.deleteAccountById(id);
  }

  @Override
  @NotNull
  public void deleteDomainById(String id)
  {
    mProvisioning.deleteDomainById(id);
  }

  @Override
  @NotNull
  public void deleteCosById(String id)
  {
    mProvisioning.deleteCosById(id);
  }

  @Override
  public Collection<Domain> getDomainAliases(Domain domain)
  {
    return mProvisioning.getDomainAliases(domain);
  }

  @Override
  public void invalidateAllCache()
  {
    mProvisioning.invalidateAllCache();
  }

  @Override
  public void purgeMemcachedAccounts(List<String> accounts)
  {
    mProvisioning.purgeMemcachedAccounts(accounts);
  }

  @Override
  public void rawQuery(String base, String query, LdapVisitor visitor)
  {
    mProvisioning.rawQuery(base, query, visitor);
  }

  @Override
  public void rawQuery(String base, String query, LdapVisitor visitor, String[] fields)
  {
    mProvisioning.rawQuery(base, query, visitor, fields);
  }

  @Override
  public int rawCountQuery(String base, String query)
  {
    return mProvisioning.rawCountQuery(base, query);
  }

  @Override
  public void registerChangePasswordListener(ChangePasswordListener listener)
  {
    mProvisioning.registerChangePasswordListener(listener);
  }

  @Override
  public void registerTwoFactorChangeListener(String name, TwoFactorAuthChangeListener listener)
  {
    mProvisioning.registerTwoFactorChangeListener(name, listener);
  }

  @Override
  public long getLastLogonTimestampFrequency()
  {
    return mProvisioning.getLastLogonTimestampFrequency();
  }

  @Override
  public Group assertGroupById(String groupId)
  {
    return mProvisioning.assertGroupById(groupId);
  }

  @Override
  public Group assertGroupByName(String groupName)
  {
    return mProvisioning.assertGroupByName(groupName);
  }
}
