# bvtest

Example showing a problem getting a `reaction` to fire as a response to a `ratom`
changing value. The example is a plain `luminus new bvtest +cljs` template project with 
BootstrapValidator and jQuery Bootstrap Wizard added.

The application found by clicking the `Weights` menu is a two-page wizard:

1. Fill in two weights:
    * _Total weight_ (max value normally 3000) 
    * _Unloaded weight_
2. Select _Eligibility_, either `A` or `B`. If `B` is selected, the maximum _Total weight_ changes 
from 3000 to 4000, using a `reaction` called `@max-total-weight`. This should in turn trigger another
`reaction` that causes a re-validation of the _Total weight_ field, but that doesn't seem to happen.
**Update:** This has been solved in commit f0c2bda which changed to a `run!` call. Thanks to @antishok
for the suggestion.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

Start figwheel from another terminal:

    lein figwheel

On the first tab, enter `3001` in _Total weight_ and `2000` in _Unloaded weight_. There should be a validation error saying
"Please enter a value between 0 and 3000". That's expected.

On the second tab, change _Eligibility_ from `A` to `B`. This should have triggered revalidation
of _Total weight_, which should in turn have changed the tab title to a green checkbox. For some
reason this doesn't happen. **Update:** Again, this has been solved in commit f0c2bda.

If we actively change to that tab, then the `onTabShow` callback fires, and he tab is revalidated,
but I can't seem to get it to happen through a `reaction`. **Update:** As I said, f0c2bda.

## License

Copyright © 2015 @ulsa
