define REDIS1_CONF
daemonize yes
port 6379
requirepass foobared
pidfile /tmp/redis1.pid
logfile /tmp/redis1.log
save ""
appendonly no
endef

export REDIS1_CONF

start: cleanup
	echo "$$REDIS1_CONF" | redis-server -

cleanup:
	- rm -vf /tmp/redis_cluster_node*.conf 2>/dev/null
	- rm dump.rdb appendonly.aof - 2>/dev/null

stop:
	kill `cat /tmp/redis1.pid`

test:
	make start
	sleep 2
	./gradlew test
	make stop

.PHONY: test
