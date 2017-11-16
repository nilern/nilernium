(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure       "1.8.0"]
                  [org.clojure/clojurescript "1.9.946" :scope "test"]

                  [adzerk/boot-cljs   "2.1.3" :scope "test"]
                  [adzerk/boot-reload "0.5.2" :scope "test"]

                  [pandeiro/boot-http "0.8.3"]])

(task-options!
  pom {:project 'nilernium
       :version "0.1.0"})

(require '[adzerk.boot-cljs   :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]

         '[pandeiro.boot-http :refer [serve]])

(deftask dev
  []
  (comp (watch)
        (reload :ids #{"js/main"})
        (cljs :ids #{"js/main"})
        (sift :to-asset #{#"^js/.*"})
        (target)
        (serve :dir "target")))
