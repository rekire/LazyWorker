LazyWorker
==========

LazyWorker is a helper class for doing tasks delayed. Such as checking inputs which require network
operations and should not been done after each key down.

## Setup (gradle based)

Edit your `build.gradle` and add this line to your dependencies:

    compile 'eu.rekisoft.android:lazyworker:2.0.0'

It should look something like this:

    dependencies {
        // other dependencies comes here
        compile 'eu.rekisoft.android:lazyworker:2.0.0'
    }

## Example
A sample implementation will follow.
