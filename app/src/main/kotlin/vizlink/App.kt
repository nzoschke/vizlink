package vizlink

fun main() {
  System.setProperty("apple.awt.UIElement", "true")
  BeatLink.getInstance().start()
}
