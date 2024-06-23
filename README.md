# first-come-first-served-event-system

## docker
### docker install
```shell
brew install docker
brew link docker
docker version
```

### run mysql using docker
```shell
docker pull mysql
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 --name mysql mysql
docker ps
docker exec -it mysql bash
```

### run redis using docker

```shell
docker pull redis
docker run -d -p 6379:6379 --name redis redis
docker ps
docker exec -it redis bash
```