package com.uber.buckcache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ignite.cache.CacheMode;
import org.jetbrains.annotations.NotNull;

public class IgniteConfig {

  @Nonnull
  private final String multicastIP;
  @Nonnull
  private final Integer multicastPort;
  @Nonnull
  private final CacheMode cacheMode;
  @Nonnull
  private final Integer cacheBackupCount;
  @Nonnull
  private final TimeUnit expirationTimeUnit;
  @Nonnull
  private final Long expirationTimeValue;
  @Nonnull
  private final Integer atomicSequenceReserveSize;
  @Nonnull
  private final List<String> hostIPs;
  @Nonnull
  private final String dnsLookupAddress;

  @JsonCreator
  public IgniteConfig(
      @Nonnull @JsonProperty("multicastIP") String multicastIP,
      @Nonnull @JsonProperty("multicastPort") Integer multicastPort,
      @Nonnull @JsonProperty("cacheMode") CacheMode cacheMode,
      @Nonnull @JsonProperty("cacheBackupCount") Integer cacheBackupCount,
      @Nonnull @JsonProperty("expirationTimeUnit") TimeUnit expirationTimeUnit,
      @Nonnull @JsonProperty("expirationTimeValue") Long expirationTimeValue,
      @Nonnull @JsonProperty("atomicSequenceReserveSize") Integer atomicSequenceReserveSize,
      @Nonnull @JsonProperty("hostIPs") List<String> hostIPs,
      @Nonnull @JsonProperty("dnsLookupAddress") String dnsLookupAddress) {
    this.multicastIP = multicastIP;
    this.multicastPort = multicastPort;
    this.cacheMode = cacheMode;
    this.cacheBackupCount = cacheBackupCount;
    this.expirationTimeUnit = expirationTimeUnit;
    this.expirationTimeValue = expirationTimeValue;
    this.atomicSequenceReserveSize = atomicSequenceReserveSize;
    this.hostIPs = hostIPs;
    this.dnsLookupAddress = dnsLookupAddress;
  }

  @NotNull
  public List<String> getHostIPs() {
    return hostIPs;
  }

  @NotNull
  public String getMulticastIP() {
    return multicastIP;
  }

  @NotNull
  public Integer getMulticastPort() {
    return multicastPort;
  }

  @NotNull
  public CacheMode getCacheMode() {
    return cacheMode;
  }

  @NotNull
  public Integer getCacheBackupCount() {
    return cacheBackupCount;
  }

  @NotNull
  public TimeUnit getExpirationTimeUnit() {
    return expirationTimeUnit;
  }

  @NotNull
  public Long getExpirationTimeValue() {
    return expirationTimeValue;
  }

  @NotNull
  public Integer getAtomicSequenceReserveSize() {
    return atomicSequenceReserveSize;
  }

  @NotNull
  public String getDnsLookupAddress() {
    return dnsLookupAddress;
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
