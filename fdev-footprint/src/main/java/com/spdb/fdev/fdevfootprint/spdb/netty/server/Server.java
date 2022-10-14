package com.spdb.fdev.fdevfootprint.spdb.netty.server;

public abstract interface Server
{
  public abstract void start();

  public abstract void restart();

  public abstract void shutdown();

  public abstract boolean isAlive();
}
