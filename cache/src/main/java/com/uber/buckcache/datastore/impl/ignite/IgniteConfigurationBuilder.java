package com.uber.buckcache.datastore.impl.ignite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.configuration.Factory;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;

import com.spotify.dns.DnsSrvResolvers;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import com.google.common.base.Strings;
import com.spotify.dns.DnsSrvResolver;
import com.spotify.dns.LookupResult;
import com.uber.buckcache.utils.BytesRateLimiter.BIT_UNIT;
import org.slf4j.Logger;

public class IgniteConfigurationBuilder {

  private IgniteConfiguration igniteConfiguration;
  private final DnsSrvResolver dnsResolver;
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IgniteConfigurationBuilder.class);

  public IgniteConfigurationBuilder() {
    this(DnsSrvResolvers.newBuilder()
        .retainingDataOnFailures(true)
        .build());
    igniteConfiguration = new IgniteConfiguration();
  }

  protected IgniteConfigurationBuilder(DnsSrvResolver dnsResolver) {
    this.dnsResolver = dnsResolver;
    igniteConfiguration = new IgniteConfiguration();
  }

  public IgniteConfigurationBuilder addMulticastBasedDiscrovery(
          String multicastIP,
          Integer multicastPort,
          List<String> hostIPs,
          String dnsLookupAddress
  ) {
    if (hostIPs == null) {
      hostIPs = new ArrayList<String>();
    }

    TcpDiscoverySpi spi = new TcpDiscoverySpi();

    TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
    ((TcpDiscoveryMulticastIpFinder) ipFinder).setMulticastGroup(multicastIP);
    ((TcpDiscoveryMulticastIpFinder) ipFinder).setMulticastPort(multicastPort);

    boolean hasDNSLookupAddress = !Strings.isNullOrEmpty(dnsLookupAddress);

    if (hasDNSLookupAddress) {
      hostIPs.addAll(this.resolveAddressByDNS(dnsLookupAddress));
    }
    ((TcpDiscoveryMulticastIpFinder) ipFinder).setAddresses(hostIPs);
    spi.setIpFinder(ipFinder);

    // Override default discovery SPI.
    igniteConfiguration.setDiscoverySpi(spi);
    return this;
  }

  public IgniteConfigurationBuilder addCacheConfiguration(CacheMode cacheMode, Integer backupCount,
      TimeUnit expirationTimeUnit, Long expirationTimeValue, String ...caches) {
    
    CacheConfiguration[] cacheConfigs = new CacheConfiguration[caches.length];
    
    for (int i = 0; i < caches.length; i++) {
      CacheConfiguration cacheConfiguration = new CacheConfiguration(caches[i]);
      cacheConfiguration.setCacheMode(cacheMode);
      cacheConfiguration.setStatisticsEnabled(true);
      cacheConfiguration.setBackups(backupCount);

      Duration expirationTime = new Duration(expirationTimeUnit, expirationTimeValue);
      Factory<ExpiryPolicy> expirePolicy = TouchedExpiryPolicy.factoryOf(expirationTime);
      cacheConfiguration.setExpiryPolicyFactory(expirePolicy);
      cacheConfiguration.setEagerTtl(true);

      cacheConfigs[i] = cacheConfiguration;
    }
    
    igniteConfiguration.setCacheConfiguration(cacheConfigs);
    return this;
  }

  public IgniteConfigurationBuilder addAtomicSequenceConfig(Integer reserveSize) {
    AtomicConfiguration atomicCfg = new AtomicConfiguration();
    atomicCfg.setAtomicSequenceReserveSize(reserveSize);
    igniteConfiguration.setAtomicConfiguration(atomicCfg);
    return this;
  }

  public IgniteConfigurationBuilder addDataStorageConfiguration(
          Boolean persistenceEnabled,
          String maxMemorySizeString,
          String pageSizeString,
          Integer emptyPagesPoolSize,
          Double evictionThreshold
  ) {
    DataStorageConfiguration storageCfg = new DataStorageConfiguration();

    storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(persistenceEnabled);
    storageCfg.getDefaultDataRegionConfiguration().setPageEvictionMode(DataPageEvictionMode.RANDOM_LRU);

    if (emptyPagesPoolSize != null && emptyPagesPoolSize > 0) {
      storageCfg.getDefaultDataRegionConfiguration().setEmptyPagesPoolSize(emptyPagesPoolSize);
    }

    if (evictionThreshold != null && evictionThreshold > 0) {
      storageCfg.getDefaultDataRegionConfiguration().setEvictionThreshold(evictionThreshold);
    }

    // uses 20% of RAM available on the local machine by default
    if (maxMemorySizeString != null && !maxMemorySizeString.isEmpty()) {
      long maxMemorySize = parseBytes(maxMemorySizeString);
      storageCfg.getDefaultDataRegionConfiguration().setMaxSize(maxMemorySize);
    }

    // 4KB by default
    if (pageSizeString != null && !pageSizeString.isEmpty()) {
      long pageSize = parseBytes(pageSizeString);
      storageCfg.setPageSize((int) pageSize);
    }

    // need to use big WAL segment size because some artifacts are huge (more than 250mb)
    //storageCfg.setWalSegmentSize((int) parseBytes("1g"));
    //storageCfg.setMaxWalArchiveSize(parseBytes("3g"));

    // or disable wal... (need to delete storage between node restarts)
    storageCfg.setWalMode(WALMode.NONE);

    // Enabling the writes throttling.
    storageCfg.setWriteThrottlingEnabled(true);

    igniteConfiguration.setDataStorageConfiguration(storageCfg);
    return this;
  }

  public IgniteConfigurationBuilder setWorkDirectory(String workDirectory) {
    if (workDirectory != null && !workDirectory.isEmpty()) {
      igniteConfiguration.setWorkDirectory(workDirectory);
    }
    return this;
  }

  public IgniteConfiguration build() {
    return igniteConfiguration;
  }

  private long parseBytes(String formattedString) {
    String multiplier = formattedString.substring(0, formattedString.length() - 1);
    String unit = formattedString.substring(formattedString.length() - 1, formattedString.length());
    return Long.parseLong(multiplier) * BIT_UNIT.valueOf(StringUtils.lowerCase(unit)).getNumberOfBytes();
  }

  private List<String> resolveAddressByDNS(String dnsLookupAddress) {
     List<String> address = new ArrayList<String>();
     for (LookupResult node : dnsResolver.resolve(dnsLookupAddress)) {
       address.add(node.host());
     }
     return address;
  }
}
