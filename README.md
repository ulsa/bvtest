# bvtest

Example showing a problem getting a `reaction` to fire inside a `document.ready`
callback. The example is a plain `luminus new bvtest +cljs` template project with 
BootstrapValidator and jQuery Bootstrap Wizard added.

The application is a two-page wizard:

1. Fill in two weights:
    * _Total weight_ (max value normally 3000) 
    * _Unloaded weight_
2. Select _Eligibility_, either `A` or `B`. If _B_ is selected, the maximum _Total weight_ changes 
from 3000 to 4000, using a `reaction` called `@max-total-weight`. This should in turn trigger another
`reaction` that causes a re-validation of the _Total weight_ field, but that doesn't seem to happen.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

Start figwheel from another terminal:

    lein figwheel

On the first tab, enter `3001` in _Total weight_ and `2000` in _Unloaded weight_. There should be a validation error saying
"Please enter a value between 0 and 3000".

On the second tab, change _Eligibility_ from `A` to `B`. This should have triggered revalidation
of _Total weight_, which should in turn have changed the tab title to a green checkbox. For some
reason this doesn't happen.

If we actively change to that tab, then the `onTabShow` callback fires, and he tab is revalidated,
but I can't seem to get it to happen through a `reaction`.

## License

Copyright © 2015 @ulsa
