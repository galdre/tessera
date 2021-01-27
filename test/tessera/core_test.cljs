(ns tessera.core-test
  (:require [tessera.core :as core]
            [cljs.test :as t :include-macros true]))

(t/deftest unit:delay*
  (let [side-effect (atom 0)
        thunk (fn [] (swap! side-effect inc) :done)
        delay (core/delay* thunk)]
    (t/is (core/tessera? delay))
    (t/is (not (core/pending? delay)))
    (t/is (not (core/failed? delay)))
    (t/is (not (core/succeeded? delay)))
    (t/is (core/redeemable? delay))
    (t/is (core/ready? delay))
    (t/is (= 0 @side-effect))
    (t/is (= :done (core/redeem delay)))
    (t/is (= 1 @side-effect))
    (t/is (= :done (core/redeem delay)))
    (t/is (= 1 @side-effect))))
