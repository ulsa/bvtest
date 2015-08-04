# bvtest

Example project used to illustrate various problems with using BootstrapValidator from Reagent.
The example is a plain `luminus new bvtest +cljs` template project with 
BootstrapValidator and jQuery Bootstrap Wizard added, and simple two-page wizard
for entering vehicle weights.

The application is found by clicking `Weights` in the top menu.

* Fill in vehicle registration number (eg 'a') and click `Get weights`.

Getting the weights from a registration number almost works, insofar as the fields will contain the
new values. The problem is that validation is not triggered when the `:value` changes. If I set the
field value using jQuery, the validation works, but then Reagent doesn't display the value. If I
set the `:value` _and_ set the value using jQuery, then I get correct validation and the value is
displayed. What is the reason for this?

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

Start figwheel from another terminal:

    lein figwheel

On the first tab, enter `a` in _Regnr_ and click _Get weights_. _Total weight_ is set to `2000` and _Unloaded weight_
is set to `1500`. The validation should be saying OK, but the validation framework still thinks the fields are empty.
Look at the code:

```clojure
(defn reset-weights [state params result max-total-weight]
  ;; enable these in order for validation to work
  #_(.val (js/$ "#in-total-weight") (:total-weight @result))
  #_(.val (js/$ "#in-unloaded-weight") (:unloaded-weight @result))

  (swap! state assoc :total-weight (:total-weight @result))
  (swap! state assoc :unloaded-weight (:unloaded-weight @result))
  (print-state 'reset-weights @state @max-total-weight)
  (reset! result nil)
  (reset! params {}))
```

Let's enable the jQuery calls that also set the value:

```clojure
(defn reset-weights [state params result max-total-weight]
  ;; enable these in order for validation to work
  (.val (js/$ "#in-total-weight") (:total-weight @result))
  (.val (js/$ "#in-unloaded-weight") (:unloaded-weight @result))

  (swap! state assoc :total-weight (:total-weight @result))
  (swap! state assoc :unloaded-weight (:unloaded-weight @result))
  (print-state 'reset-weights @state @max-total-weight)
  (reset! result nil)
  (reset! params {}))
```

Now everything works as it should. But why would I need to set the value both ways?

## License

Copyright © 2015 @ulsa
