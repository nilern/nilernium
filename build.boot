(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[perun "0.2.1-SNAPSHOT"]
                  [pandeiro/boot-http "0.6.3-SNAPSHOT"]])

(task-options!
  pom {:project 'nilernium
       :version "0.1.0"})

(require '[io.perun :refer :all]
         '[pandeiro.boot-http :refer [serve]])

(deftask build-dev
  "Build dev version"
  []
  (comp (global-metadata)
        (base)
        (markdown)
        (collection :renderer 'nilernium.core/render :page "index.html")))

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
