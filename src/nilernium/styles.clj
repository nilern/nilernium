(ns nilernium.styles
  (:require [garden.units :as u]
            [garden.selectors :refer [checked]]
            [garden.def :refer [defstyles]]))

(def page-width (u/em 50))

(defstyles main
  ;;;; Boilerplate

  ["*" {:box-sizing :border-box}] ; reasonable widths & heights

  ;;;; General Layout

  ["body"
   {:display :table
    :max-width page-width
    :margin [[0 "auto"]]
    :background-color "grey"}] ; for debugging

  ["header, #content"
   {:display :table-row
    :background-color "white"}]

  ["header > *, nav, article"
   {:display :table-cell}]

  ;;;; Spacing

  ["#site-title, nav"
   {:padding [[0 (u/em 1) 0 (u/em 2)]]}]

  ["#page-title, article"
   {:padding [[0 (u/em 2) 0 (u/em 1)]]}]

  ["pre"
   {:margin-left (u/em 2)}]

  ;;;; General Typography

  ["body"
   {:font-family ["Alegreya" "serif"]
    :font-size (u/px 18)}]

  ["h1, h2, h3, h4, h5, h6"
   {:font-family ["Eagle Lake" "fantasy"]}]

  ["p"
   {:margin 0}
   ["+ p"
    {:text-indent (u/em 1)}]]

  ["pre"
   {:font-family ["Inconsolata" "monospace"]}]

  ;;;; Header

  ["#site-title"
   {:color "red"
    :text-align :center}]

  ["#page-tags"
   {:padding 0
    :display :inline}
   ["li"
    {:display :inline
     :list-style :none}
    ["+ li::before" ; that is, insert between
     {:content "', '"}]]]

  ;;;; Navigation

  ["nav > ul"
   {:margin 0
    :padding 0}
   ["> li"
    {:padding [[(u/ex 1) (u/em 1)]]
     :list-style :none
     :border [[(u/px 1) :solid "lightGrey"]]
     :border-right :none}
    ["+ li"
     {:border-top :none}] ; avoid doubling "internal" borders

    ["input[type=checkbox]"
     {:display :none}]
    ["input[type=checkbox] ~ ul"
     {:display :none}]
    ["input[type=checkbox]:checked ~ ul"
     {:display :block}]]

   ["> li:first-of-type"
    {:border-radius [[(u/ex 1) 0 0 0]]}]

   ["> li:last-of-type"
    {:border-radius [[0 0 0 (u/ex 1)]]}]])
