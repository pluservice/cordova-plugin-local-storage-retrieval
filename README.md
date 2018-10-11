# cordova-plugin-localstorage-retrieval

### Usage

```javascript
document.addEventListener("deviceready", function() {
  window.cordova.plugins.LocalStorageRetrieval.getLocalStorageData(
    "file://_/example.html",
    function onSuccess(localStorageString) {
      var parsed = JSON.parse(localStorageString);

      // use `parsed`
    },
    function onError(error) {
      // handle error
    }
  );
});
```
