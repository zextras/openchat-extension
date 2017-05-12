#!/bin/bash

mkdir -p src/java/com/zextras/lib
cat > src/java/com/zextras/lib/BuildInfo.java <<EOF

package com.zextras.lib;

public class BuildInfo
{
	public final static String COMMIT="$(git rev-parse HEAD)";
	public final static String Version="$(cat version)";
};

EOF
