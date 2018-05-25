# LightICE

Health care monitor-agnostic data collection software (high and low frequency data).

This project is part of the largest [Pancarte project](https://pancarte.eds.ovh).

For now, it only works with:

* Philips MPxx (via RS-232)
* Philips MPxx (via Ethernet)

## Running it

### Launch the Main 

#### Configuration

You should create a `conf.json` file in the current directory. An example of the configuration is available in example.conf.json.

#### Linux

```commandline
./gradlew run
```

Have fun!

### Building a .jar (if you know what you are doing)


```commandline
./gradlew publishToMavenLocal
ls build/libs/
```

Exemples are available here:

* [Kotlin to zeromq](https://github.com/jaj42/IntelliPhynet)


## Development

This project accepts Pull/Requests and we will be happy to answer your issues with LightICE.
To add a new monitor in the list of supported monitors, we need a monitor to test it, this also implies time, if you need a monitor that is not currently supported, don't hesitate to participate in this project.

## Licence

This project is a light version of [MD PnP OpenICE](https://github.com/mdpnp/mdpnp).
It is [DDS](https://en.wikipedia.org/wiki/Data_Distribution_Service)-free (as opposed to OpenICE).

The LICENCE: [MDPNP Licence](MDPNP_LICENCE)
