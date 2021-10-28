# LazyWorker ![Maven Central](https://img.shields.io/maven-central/v/eu.rekisoft.android.util/LazyWorker)

LazyWorker is a helper class for doing tasks delayed. Such as checking inputs which require network
operations and should not been done after each key down.

## Setup (gradle based)

Edit your `build.gradle` and add this line to your dependencies:

    implementation 'eu.rekisoft.android:lazyworker:2.0.3'

It should look something like this:

    implementation {
        // other dependencies comes here
        implementation 'eu.rekisoft.android:lazyworker:2.0.3'
    }

## Example
Here is a sample for implementing a lifecycle aware countdown timer in Kotlin:

    val countdown = LazyWorker.createLifeCycleAwareJob(viewLifecycleOwner.lifecycle) {
        val left = expiresAt - System.currentTimeMillis()
        val remaining = "%d:%02d".format(
            TimeUnit.MILLISECONDS.toMinutes(left),
            TimeUnit.MILLISECONDS.toSeconds(left % 60000)
        )
        println(remaining)
        if (expiresAt > System.currentTimeMillis()) {
            doLater(1000)
        } else {
            println("Time expired")
        }
    }

    // Start timer
    countdown.doNow()