# OpenICE Light

This is a light version of [https://github.com/mdpnp/mdpnp](https://github.com/mdpnp/mdpnp).
It is DDS-free and aims to only use the Java standard library. However, some useful classes of DDS have been copied for this project.

For now, it only works with:

* Philips Intellivue (via RS-232)

Features:

* [x] Listen multiple devices at the same time
* [x] An API to easily export data from devices
* [x] Write to Kafka (using the API)
* [x] Write to STDOUT (using the API)

## Running it

### Configuration

You should create a `conf.json` file in the current directory. An example of the configuration is available in example.conf.json.

### Linux

```
./gradlew run
```

Have fun!

## Development

### Create avro serializer

```
wget http://apache.crihan.fr/dist/avro/avro-1.8.2/java/avro-tools-1.8.2.jar;
java -jar avro-tools-1.8.2.jar compile schema resources/records.avsc src/
```

## Licence

* [MDPNP Licence](https://github.com/mdpnp/mdpnp/blob/master/interop-lab/demo-apps/LICENSE.txt)
