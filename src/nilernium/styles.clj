(ns nilernium.styles
  (:require [garden.units :as u]
            [garden.def :refer [defstyles]]))

(def page-width (u/px 1000))

(def nav-width (u/px 250))

(defstyles main
  ["*" {:box-sizing "border-box"}] ; reasonable widths & heights

  ["header, article, nav"
   {:background-color "white"
    :border [[(u/px 1) "solid" "lightGrey"]]}] ; for debugging

  [:body
   {:margin 0
    :background-color "grey"}] ; for debugging

  ["header, article"
   {:width (u/percent 100)
    :max-width page-width
    :margin [[0 "auto"]]}] ; center it

  ["nav, #site-title"
   {:width nav-width
    :float "left"}]

  [".clearfix" {:clear "both"}]) ; good ol' clearfix hack
