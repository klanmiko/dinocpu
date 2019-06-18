// Control logic for the processor

package dinocpu

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Output: branch, true if branch or jump and link register (jal). update PC with immediate
 * Output: memread, true if we should read from memory
 * Output: toreg, 0 for writing ALU result, 1 for writing memory data, 2 for writing pc + 4
 * Output: add, true if the ALU should add the results
 * Output: memwrite, true if writing to the data memory
 * Output: regwrite, true if writing to the register file
 * Output: immediate, true if using the immediate value
 * Output: alusrc1, 0 for read data 1, 1 for the constant zero, 2 for the PC
 * Output: jump, 0 for no jump, 2 for jal (jump and link), 3 for jalr (jump and link register)
 *
 * For more information, see section 4.4 of Patterson and Hennessy.
 * This follows figure 4.22.
 */

class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val validinst = Output(Bool())
    val branch = Output(Bool())
    val memread = Output(Bool())
    val toreg = Output(UInt(2.W))
    val add = Output(Bool())
    val memwrite = Output(Bool())
    val regwrite = Output(Bool())
    val immediate = Output(Bool())
    val alusrc1 = Output(UInt(2.W))
    val jump    = Output(UInt(2.W))
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, false.B, 4.U,   false.B, false.B,  false.B, false.B,    0.U,    0.U),
      Array(              /*  valid instr, branch,  memread, toreg, add,   memwrite, immediate, regwrite, alusrc1,  jump */
      // R-format
      BitPat("b0110011") -> List(true.B, false.B, false.B, 0.U,   false.B, false.B,  false.B, true.B,     0.U,    0.U),
      // I-format
      BitPat("b0010011") -> List(true.B, false.B, false.B, 0.U,   false.B, false.B,  true.B,  true.B,     0.U,    0.U),
      // load
      BitPat("b0000011") -> List(true.B, false.B, true.B,  1.U,   true.B,  false.B,  true.B,  true.B,     0.U,    0.U),
      // store
      BitPat("b0100011") -> List(true.B, false.B, false.B, 0.U,   true.B,  true.B,   true.B,  false.B,    0.U,    0.U),
      // beq
      BitPat("b1100011") -> List(true.B, true.B,  false.B, 0.U,   false.B, false.B,  false.B, false.B,    0.U,    0.U),
      // lui
      BitPat("b0110111") -> List(true.B, false.B, false.B, 0.U,   true.B,  false.B,  true.B,  true.B,     1.U,    0.U),
      // auipc
      BitPat("b0010111") -> List(true.B, false.B, false.B, 0.U,   true.B,  false.B,  true.B,  true.B,     2.U,    0.U),
      // jal
      BitPat("b1101111") -> List(true.B, false.B, false.B, 2.U,   false.B, false.B,  false.B, true.B,     1.U,    2.U),
      // jalr
      BitPat("b1100111") -> List(true.B, false.B, false.B, 2.U,   false.B, false.B,  true.B,  true.B,     0.U,    3.U),
      //csr
      BitPat("b1110011") -> List(true.B, false.B, false.B, 3.U,   false.B, false.B,  false.B, false.B,    0.U,    0.U),
      //fence
      BitPat("b0001111") -> List(true.B, false.B, false.B, 4.U,   false.B, false.B,  false.B, false.B,    0.U,    0.U)
      ) // Array
    ) // ListLookup

  io.validinst := signals(0)
  io.branch := signals(1)
  io.memread := signals(2)
  io.toreg := signals(3)
  io.add := signals(4)
  io.memwrite := signals(5)
  io.immediate := signals(6)
  io.regwrite := signals(7)
  io.alusrc1 := signals(8)
  io.jump := signals(9)
}
