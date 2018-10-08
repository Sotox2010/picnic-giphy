# Picnic Giphy Test

## Download

You can download the APK file for this assignment here: [Download APK](https://www.dropbox.com/s/rk33run0uf73fjl/app-debug.apk)

## Architecture

The architecture chosen for this assignment is _*Model-View-ViewModel*_ (MVVM). This is a reactive
architectural pattern that suits very well the Android platform by providing clean and understandable structure that embraces the 
separation of concerns. The reason behind this decision (given the complexity and size of the project)
is the flexibility and extensibility this architecture offers, with minimum a mount of boilerplate
it allows to iterate and make progress much faster than other architectures.

THe Model layer is comprised by the repositories and data sources, suck as `GifRepository` and
`GifPageKeyedDataSource`. This layer main task is to manage the app's data, for example like
retrieving data from one or many data source.

Classes that inherits from `ViewModel` belongs to the ViewModel layer. This layer main task is to
execute the business logic, request data from the repositories and prepare it for the View to process.
`GifStreamViewModel` and `GifDetailViewModel` are examples of ViewModels

Last, the View layer contains all classes that inherits from `Activity`/`Fragment`. These classes
main task is to render the information on the screen and inform the ViewModel about user
interactions events (like button clicks). `GifStreamActivity` and `GifDetailActivity` belong to
this layer.

## Chosen Gif format

As you stated in the assignment text, Giphy's API provides the images in three different formats.
Fot this task, I chose the `.gif` format. This is because I used an utility library for image loading
called *Glide* that offers native support for the `.gif` format, but more than that, it offers mechanisms
for caching, threading and auto-cancel of unused request, making the gif loading very easy and
efficient for such a constrained environment as a mobile device.

## About random gif loading

When opening a Gif from the trending stream You will see the fully sized gif plus a tiny random gif
that regenerates every 10 seconds. However, this random gif is not clickable, and the reason for
this is that I was not sure about how would you like me to deal with the navigation in that case;
Either keep a backstack of gif activities or just replace the current gif details for the tapped random gif    

## Where to insert the API key

For the reviewer: in order to be able to run the project properly, you must add the Giphy API
key in the line number `19` of the following file:
 
[`build.gradle`][1]


[1]: app/build.gradle

## For further improvements

- Implement Dependency Injection.
- Animations between screens.
- Unit tests (mainly for UI, Repositories and ViewModels.)
