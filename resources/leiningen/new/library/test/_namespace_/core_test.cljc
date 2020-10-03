(ns {{namespace}}.core-test
  (:require #?(:clj [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :include-macros true :refer [deftest testing is]])))


(deftest a-test
  (testing "FIXME, I don't fail."
    (is (true? (= 4 (count (str (= 1 1))))))))
