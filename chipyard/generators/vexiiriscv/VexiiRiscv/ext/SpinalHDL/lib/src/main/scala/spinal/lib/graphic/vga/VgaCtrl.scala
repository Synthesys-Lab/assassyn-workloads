package spinal.lib.graphic.vga

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.BusSlaveFactory
import spinal.lib.experimental.bus.neutral.NeutralStreamDma
import spinal.lib.eda.altera.QSysify
import spinal.lib.graphic.{RgbConfig, Rgb}


object VgaTimingsHvScala{
  def craft(sync: Int, front: Int, pix: Int, post: Int) = new VgaTimingsHvScala(
    syncStart = sync - 1,
    colorStart = sync + front - 1,
    colorEnd = sync + front + pix - 1,
    syncEnd = sync + front + pix + post - 1,
    polarity = false
  )

  def timing(pixels: Int,
             sync: Int,
             front: Int,
             back: Int,
             polarity: Boolean) = new VgaTimingsHvScala(
    syncStart = sync - 1,
    colorStart = sync + back - 1,
    colorEnd = sync + back + pixels - 1,
    syncEnd = sync + back + pixels + front - 1,
    polarity = polarity
  )
}

class VgaTimingsHvScala(val syncStart: Int,
                        val syncEnd: Int,
                        val colorStart: Int,
                        val colorEnd: Int,
                        val polarity: Boolean)

class VgaTimingsScala(val h : VgaTimingsHvScala,
                      val v : VgaTimingsHvScala)

object VgaTimingsScala{
  def h640_v480_r60 = {
    new VgaTimingsScala(
      h = new VgaTimingsHvScala(
        syncStart = 95,
        syncEnd = 799,
        colorStart = 143,
        colorEnd = 783,
        polarity = false
      ),
      v = new VgaTimingsHvScala(
        syncStart = 1,
        syncEnd = 524,
        colorStart = 34,
        colorEnd = 514,
        polarity = false
      ),
    )
  }

  def h800_v600_r60 = new VgaTimingsScala(
    h = VgaTimingsHvScala.timing(
      pixels = 800,
      sync = 128,
      front = 40,
      back = 88,
      polarity = true
    ),
    v = VgaTimingsHvScala.timing(
      pixels = 600,
      sync = 4,
      front = 1,
      back = 23,
      polarity = true
    )
  )

  def h128_v128_r60 = new VgaTimingsScala(
    h = VgaTimingsHvScala.craft(10,15,128,5),
    v = VgaTimingsHvScala.craft(5,2,128,2)
  )

  def h64_v64_r60 = new VgaTimingsScala(
    h = VgaTimingsHvScala.craft(10, 15, 64, 5),
    v = VgaTimingsHvScala.craft(5, 2, 64, 2)
  )
}

//class VgaTimingsHvScala(){
//  var syncStart = 0
//  var syncEnd = 0
//  var colorStart = 0
//  var colorEnd = 0
//  var polarity = 0
//}
//class VgaTimingsScala(){
//  var h = new VgaTimingsHvScala()
//  var v = new VgaTimingsHvScala()
//}


case class VgaTimingsHV(timingsWidth: Int) extends Bundle {
  val syncStart = UInt(timingsWidth bit)
  val syncEnd = UInt(timingsWidth bit)
  val colorStart = UInt(timingsWidth bit)
  val colorEnd = UInt(timingsWidth bit)
  val polarity = Bool()

  def assign(t : VgaTimingsHvScala): Unit = {
    syncStart := t.syncStart
    syncEnd := t.syncEnd
    colorStart := t.colorStart
    colorEnd := t.colorEnd
    polarity := Bool(t.polarity)
  }
}

case class VgaTimings(timingsWidth: Int) extends Bundle {
  val h = VgaTimingsHV(timingsWidth)
  val v = VgaTimingsHV(timingsWidth)

  def assign(t: VgaTimingsScala): Unit = {
    h.assign(t.h)
    v.assign(t.v)
  }

  def setAs_h640_v480_r60: Unit = {
    h.syncStart := 95
    h.syncEnd := 799
    h.colorStart := 143
    h.colorEnd := 783
    v.syncStart := 1
    v.syncEnd := 524
    v.colorStart := 34
    v.colorEnd := 514
    h.polarity := False
    v.polarity := False
  }
  def setAs_h64_v64_r60: Unit = {
    h.syncStart := 96 - 1
    h.syncEnd := 800 - 1
    h.colorStart := 96 + 16 - 1 + 288
    h.colorEnd := 800 - 48 - 1 - 288
    v.syncStart := 2 - 1
    v.syncEnd := 525 - 1
    v.colorStart := 2 + 10 - 1 + 208
    v.colorEnd := 525 - 33 - 1 - 208
    h.polarity := False
    v.polarity := False
  }

  def setAs(hPixels : Int,
            hSync : Int,
            hFront : Int,
            hBack : Int,
            hPolarity : Boolean,
            vPixels : Int,
            vSync : Int,
            vFront : Int,
            vBack : Int,
            vPolarity : Boolean): Unit = {
    h.syncStart := hSync - 1
    h.colorStart := hSync + hBack - 1
    h.colorEnd := hSync + hBack + hPixels - 1
    h.syncEnd := hSync + hBack + hPixels + hFront - 1
    v.syncStart := vSync - 1
    v.colorStart := vSync + vBack - 1
    v.colorEnd := vSync + vBack + vPixels - 1
    v.syncEnd := vSync + vBack + vPixels + vFront - 1
    h.polarity := Bool(hPolarity)
    v.polarity := Bool(vPolarity)
  }


  def setAs_h1920_v1080_r60: Unit = setAs(
    hPixels    = 1920,
    hSync      = 44,
    hFront     = 88,
    hBack      = 148,
    hPolarity  = true,
    vPixels    = 1080,
    vSync      = 5,
    vFront     = 4,
    vBack      = 36,
    vPolarity  = true
  )

  def setAs_h800_v600_r60: Unit = setAs(
    hPixels    = 800,
    hSync      = 128,
    hFront     = 40,
    hBack      = 88,
    hPolarity  = true,
    vPixels    = 600,
    vSync      = 4,
    vFront     = 1,
    vBack      = 23,
    vPolarity  = true
  )


  def driveFrom(busCtrl : BusSlaveFactory,baseAddress : Int) : Unit = {
    require(busCtrl.busDataWidth == 32)

    busCtrl.drive(h.syncStart  ,baseAddress +  0)
    busCtrl.drive(h.syncEnd    ,baseAddress +  4)
    busCtrl.drive(h.colorStart ,baseAddress +  8)
    busCtrl.drive(h.colorEnd   ,baseAddress + 12)
    busCtrl.drive(v.syncStart  ,baseAddress + 16)
    busCtrl.drive(v.syncEnd    ,baseAddress + 20)
    busCtrl.drive(v.colorStart ,baseAddress + 24)
    busCtrl.drive(v.colorEnd   ,baseAddress + 28)
    busCtrl.drive(h.polarity   ,baseAddress + 32, 0) init(False)
    busCtrl.drive(v.polarity   ,baseAddress + 32, 1) init(False)
  }
}


object VgaTimingPrint extends App{
  def show( hPixels : Int,
            hSync : Int,
            hFront : Int,
            hBack : Int,
            hPolarity : Boolean,
            vPixels : Int,
            vSync : Int,
            vFront : Int,
            vBack : Int,
            vPolarity : Boolean): Unit = {
    val h_syncStart = hSync - 1
    val h_colorStart = hSync + hBack - 1
    val h_colorEnd = hSync + hBack + hPixels - 1
    val h_syncEnd = hSync + hBack + hPixels + hFront - 1
    val v_syncStart = vSync - 1
    val v_colorStart = vSync + vBack - 1
    val v_colorEnd = vSync + vBack + vPixels - 1
    val v_syncEnd = vSync + vBack + vPixels + vFront - 1

    println(
      s"""    .hSyncStart  = $h_syncStart,
         |    .hSyncEnd    = $h_syncEnd,
         |    .hColorStart = $h_colorStart,
         |    .hColorEnd   = $h_colorEnd,
         |    .vSyncStart  = $v_syncStart,
         |    .vSyncEnd 	 = $v_syncEnd,
         |    .vColorStart = $v_colorStart,
         |    .vColorEnd 	 = $v_colorEnd,
         |    .polarities  = ${(if(hPolarity) 1 else 0) | (if(vPolarity) 2 else 0)},
         |""".stripMargin)
  }

  show(
    hPixels    = 640,
    hSync      = 96,
    hFront     = 16,
    hBack      = 48,
    hPolarity  = false,
    vPixels    = 480,
    vSync      = 2,
    vFront     = 10,
    vBack      = 33,
    vPolarity  = false
  )
}

case class VgaCtrl(rgbConfig: RgbConfig, timingsWidth: Int = 12) extends Component {
  val io = new Bundle {
    val softReset = in Bool() default(False)
    val timings   = in(VgaTimings(timingsWidth))

    val frameStart = out Bool()
    val pixels     = slave Stream (Rgb(rgbConfig))
    val vga        = master(Vga(rgbConfig))

    val error      = out Bool()
  }

  case class HVArea(timingsHV: VgaTimingsHV, enable: Bool) extends Area {
    val counter = Reg(UInt(timingsWidth bit)) init(0)

    val syncStart = counter === timingsHV.syncStart
    val syncEnd = counter === timingsHV.syncEnd
    val colorStart = counter === timingsHV.colorStart
    val colorEnd = counter === timingsHV.colorEnd
    val polarity = timingsHV.polarity

    when(enable) {
      counter := counter + 1
      when(syncEnd) {
        counter := 0
      }
    }

    val sync    = RegInit(False) setWhen(syncStart) clearWhen(syncEnd)
    val colorEn = RegInit(False) setWhen(colorStart) clearWhen(colorEnd)

    when(io.softReset) {
      counter := 0
      sync := False
      colorEn := False
    }
  }

  val h = HVArea(io.timings.h, True)
  val v = HVArea(io.timings.v, h.syncEnd) // h.colorEnd
  val colorEn = h.colorEn && v.colorEn
  io.pixels.ready := colorEn || io.softReset
  io.error := colorEn && !io.pixels.valid

  io.frameStart := v.syncStart && h.syncStart

  io.vga.hSync := h.sync ^ h.polarity
  io.vga.vSync := v.sync ^ v.polarity
  io.vga.colorEn := colorEn
  io.vga.color := io.pixels.payload


  //Can be called by parent component to make the VgaCtrl autonom by using a Stream of fragment to feed it.
  def feedWith(that : Stream[Fragment[Rgb]], resync : Bool = False) = new Area{
    val error = RegInit(False)
    val waitStartOfFrame = RegInit(False)
    val firstPixel = Reg(Bool()) setWhen(io.frameStart) clearWhen(that.firstFire)

    io.pixels << that.toStreamOfFragment.haltWhen(waitStartOfFrame && !error)

    when(io.frameStart){
      waitStartOfFrame := False
    }
    when(that.fire && that.last){
      error := False
      waitStartOfFrame := error
    }
    when(!waitStartOfFrame && !error) {
      when(io.error || resync || firstPixel && that.valid && !that.first) {
        error := True
      }
    }
  }
}


object VgaCtrl {
  def main(args: Array[String]) {
    SpinalVhdl(new VgaCtrl(RgbConfig(8, 8, 8)))
  }
}

//TODO add to doc example
class BlinkingVgaCtrl(rgbConfig: RgbConfig) extends Component{
  val io = new Bundle{
    val vga = master(Vga(rgbConfig))
  }

  val counter = Reg(UInt(rgbConfig.gWidth bits))
  val ctrl = new VgaCtrl(rgbConfig)
  ctrl.io.softReset := False
  ctrl.io.timings.setAs_h640_v480_r60
  ctrl.io.pixels.valid := True
  ctrl.io.pixels.r := 0
  ctrl.io.pixels.g := counter
  ctrl.io.pixels.b := 0
  ctrl.io.vga <> io.vga

  when(ctrl.io.frameStart){
    counter := counter + 1
  }
}

object BlinkingVgaCtrl {
  def main(args: Array[String]) {
    SpinalVhdl(new BlinkingVgaCtrl(RgbConfig(8, 8, 8))).toplevel
  }
}