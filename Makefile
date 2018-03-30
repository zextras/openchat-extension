all: dist/openchat.jar

.PHONY: clean install guard-%

lib/zal.jar:
	ant download-zal

dist/openchat.jar: lib/zal.jar
	ant

clean:
	ant clean

install: guard-ZIMLET_DEV_SERVER \
		lib/zal.jar \
		dist/openchat.jar
	# Deploy the zimlet on a server
	ssh root@${ZIMLET_DEV_SERVER} "mkdir -p /opt/zimbra/lib/ext/openchat"
	scp lib/zal.jar root@${ZIMLET_DEV_SERVER}:/opt/zimbra/lib/ext/openchat
	scp dist/openchat.jar root@${ZIMLET_DEV_SERVER}:/opt/zimbra/lib/ext/openchat
	# ssh root@${ZIMLET_DEV_SERVER} "chown -R zimbra:zimbra /opt/zimbra/lib/ext/openchat"
	ssh root@${ZIMLET_DEV_SERVER} "su - zimbra -c '/opt/zimbra/bin/zmmailboxdctl restart'"
	echo -n "Completed @ " && date

guard-%:
	# Verify if an environment variable is set
	@ if [ "${${*}}" = "" ]; then \
		echo "Environment variable $* not set"; \
		exit 1; \
	fi
