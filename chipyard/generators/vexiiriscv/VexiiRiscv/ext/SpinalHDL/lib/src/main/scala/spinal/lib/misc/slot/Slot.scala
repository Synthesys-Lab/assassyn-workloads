package spinal.lib.misc.slot

import spinal.core._
import spinal.lib._

class Slot extends Area {
  val fire = False
  val valid = RegInit(False) clearWhen (fire)
}

class SlotPool[T <: Slot](slotsCount: Int)(gen: => T) extends Area {
  val slots = for (i <- 0 until slotsCount) yield gen
  val allocate = new Area {
    val full = slots.map(_.valid).andR
    val oh = B(OHMasking.firstV2(Vec(slots.map(!_.valid))))
    val id = OHToUInt(oh)
    def apply(body : T => Unit ){
      slots.onMask(oh){s => s.valid := True; body(s)}
    }
  }
  def free(id : UInt) = slots.onSel(id)(_.fire := True)
}
