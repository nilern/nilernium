(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.7.0"]
                  [perun "0.2.1-SNAPSHOT"]
                  [enlive "1.1.6"]
                  [garden "1.3.2"]
                  [org.martinklepsch/boot-garden "1.3.2-0"]
                  [pandeiro/boot-http "0.6.3-SNAPSHOT"]])

(task-options!
  pom {:project 'nilernium
       :version "0.1.0"})

(require '[io.perun :refer :all]
         '[org.martinklepsch.boot-garden :refer [garden]]
         '[pandeiro.boot-http :refer [serve]])

(deftask build-dev
  "Build dev version"
  []
  (comp (garden :styles-var 'nilernium.styles/main
                :output-to "public/main.css")
        (base)
        (markdown)
        (global-metadata)
        (render :renderer 'nilernium.core/render
                :filterer #(not (.contains (:path %) "templates/")))))

(deftask dev
  []
  (comp (watch)
        (build-dev)
        (serve :resource-root "public")))

(deftask prod
  []
  (comp (build-dev)
        (sift :include #{#"^public"})
        (sift :move {#"^public/" ""})
        (target :dir #{"build"})))
