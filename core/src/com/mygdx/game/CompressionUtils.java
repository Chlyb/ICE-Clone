package com.mygdx.game;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
public class CompressionUtils {

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_SPEED);
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static float toFloat32(short float16)
    {
        int nHalf = (int) float16;

        int S = (nHalf >>> 15) & 0x1;
        int E = (nHalf >>> 10) & 0x1F;
        int T = (nHalf       ) & 0x3FF;

        E = E == 0x1F
                ? 0xFF  // it's 2^w-1; it's all 1's, so keep it all 1's for the 32-bit float
                : E - 15 + 127;     // adjust the exponent from the 16-bit bias to the 32-bit bias

        // sign S is now bit 31
        // exp E is from bit 30 to bit 23
        // scale T by 13 binary digits (it grew from 10 to 23 bits)
        return Float.intBitsToFloat(S << 31 | E << 23 | T << 13);
    }

    public static short toFloat16( float fval )
        {
        int fbits = Float.floatToIntBits( fval );
        int sign = fbits >>> 16 & 0x8000;          // sign only
        int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

        if( val >= 0x47800000 )               // might be or become NaN/Inf
        {                                     // avoid Inf due to rounding
            if( ( fbits & 0x7fffffff ) >= 0x47800000 )
            {                                 // is or must become NaN/Inf
                if( val < 0x7f800000 )        // was value but too large
                    return (short) (sign | 0x7c00);     // make it +/-Inf
                return (short) (sign | 0x7c00 |        // remains +/-Inf or NaN
                        ( fbits & 0x007fffff ) >>> 13); // keep NaN (and Inf) bits
            }
            return (short) (sign | 0x7bff);             // unrounded not quite Inf
        }
        if( val >= 0x38800000 )               // remains normalized value
            return (short) (sign | val - 0x38000000 >>> 13); // exp - 127 + 15
        if( val < 0x33000000 )                // too small for subnormal
            return (short) sign;                      // becomes +/-0
        val = ( fbits & 0x7fffffff ) >>> 23;  // tmp exp for subnormal calc
        return (short) (sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
                + ( 0x800000 >>> val - 102 )     // round depending on cut off
                >>> 126 - val ));   // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
    }
}