# kotlin-recyclerview-remote

This is a simple project intended as a learning exercise to get better acquainted with the Kotlin language.  It's a fairly simple application that fetches data from a remote endpoint and displays this data to the user.  The returned data (in JSON format) contains an array of artist names and urls to images of their photos.  The application displays the image along with the name of the artist, some info about the photo, and an option to save images to "favorites".  These favorites persist on the device across sessions.  This sample project makes use of the following:

- RecyclerView implementation to efficiently display row items
- Volley to make http requests
- Glide library to quickly render .jpg images from a remote endpoint
- SharedPreferences API to save key/value data local storage
- A few simple animations. The first is the expansion of a card when clicked.  The other is when a card is removed from the favorites collection.  
- Material design library components like Cards, Buttons, Icons, etc.
