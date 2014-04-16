package utils;

/**
 * Created by sviatik on 3/17/14.
 */

// 0x hex

// op1 & op2    bitwise and

// op1 >> op2   shift bits of op1 right by distance op2

// op1 << op2   shift bits of op1 left by distance op2

// op1 >>> op2  shift bits of op1 right by distance of op2 (unsigned)

public class Pixel {

    static public int getAlpha( int p ) {

        return (p & 0xff000000) >>> 24;

    }



    static public int getRed( int p ) {

        return (p & 0xff0000) >> 16;

    }



    static public int getGreen( int p ) {

        return (p & 0xff00) >> 8;

    }



    static public int getBlue( int p ) {

        return (p & 0xff);

    }



    static public int createRGB( int a, int r, int g, int b ) {

        return ((a & 0xff) << 24) |

                ((r & 0xff) << 16) |

                ((g & 0xff) << 8) |

                (b & 0xff);

    }



    // create packed int pixel from red, grren, and blue components

    static public int createRGB( int r, int g, int b ) {

        return ((r & 0xff) << 16) |

                ((g & 0xff) << 8) |

                (b & 0xff);

    }

}
