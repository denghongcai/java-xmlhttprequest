# java-xmlhttprequest

This component provides a basic set of JS functions which can be used within Nashorn to 
provide some scripting which involves remote requests.

This code was originally drawn from https://gist.github.com/bripkens/8597903 and 
adapted in a number of ways.

1. Switched to use the Apache HTTP components
2. Changed error handling so that errors in timeouts cancel everything

As with the original implementation, this is lamentably incomplete as an 
implementation of XMLHttpRequest, but it ought to be enough to get by with if 
you only need the most common use cases. And besides, this is primarily intended
for embedding, so we don't really need to be an exact model of XMLHttpRequest
anyway, as that would prevent us from using authentication, for example. 


### Examples

See the testing components for examples of use. 


### License

Good question. There wasn't one on the original gist, but most of it has changed
anyway. I'd suggest BSD. 