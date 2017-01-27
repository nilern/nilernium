(ns nilernium.styles
  (:require [garden.units :as u]
            [garden.selectors :refer [checked]]
            [garden.def :refer [defstyles]]))

(def page-width (u/px 1000))

(def nav-width (u/px 250))

(def header-height (u/px 150))

(defstyles main
  ["*" {:box-sizing :border-box}] ; reasonable widths & heights

  ["header, article, nav"
   {:background-color "white"
    :border [[(u/px 1) :solid "lightGrey"]]}] ; for debugging

  ["body"
   {:margin 0
    :background-color "grey"}] ; for debugging

  ["header, article"
   {:width (u/percent 100)
    :max-width page-width
    :margin [[0 "auto"]]}] ; center it

  ["nav, #site-title"
   {:width nav-width
    :float :left}]

  ["#site-title"
   {:color "red"
    :text-align :center}]

  ["#page-title"
   {:float :left}]

  ["header, header > *"
   {:height header-height}]

  [".clearfix" {:clear :both}] ; good ol' clearfix hack

  ["#page-tags"
   {:padding 0
    :display :inline}
   ["li"
    {:display :inline
     :list-style :none}
    ["+ li::before" ; that is, insert between
     {:content "', '"}]]]

  ["nav li"
   ["input[type=checkbox]"
    {:display :none}]
   ["input[type=checkbox] ~ ul"
    {:display :none}]
   ["input[type=checkbox]:checked ~ ul"
    {:display :block}]])
