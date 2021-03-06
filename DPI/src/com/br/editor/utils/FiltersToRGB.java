package com.br.editor.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by r028367 on 31/03/2017.
 */
public class FiltersToRGB {

    private BufferedImage bufferedImage;
    private int widthImage, heightImage;

    public int[][] getMatrixPixels() {
        return matrixPixels;
    }

    private int [][] matrixPixels;
    private CallbackApplyFilter callbackApplyFilter;

    private final void createInstance(BufferedImage bufferedImage) {
        this.bufferedImage  = bufferedImage;
        this.widthImage     = bufferedImage.getWidth();
        this.heightImage    = bufferedImage.getHeight();
        this.matrixPixels   = new int[heightImage][widthImage];
        for(int i=0; i<heightImage; i++){
            for (int j=0; j<widthImage; j++) {
                int c = bufferedImage.getRGB(j, i);
                //Color color = new Color(c);
                //int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                matrixPixels[i][j] = c;
            }
        }
    }

    public void redefineBufferedImage(BufferedImage bufferedImage) {
        createInstance(bufferedImage);
    }

    public FiltersToRGB(BufferedImage bufferedImage) {
        createInstance(bufferedImage);
    }

    public FiltersToRGB(BufferedImage bufferedImage, CallbackApplyFilter callbackApplyFilter) {
        createInstance(bufferedImage);
        this.callbackApplyFilter = callbackApplyFilter;
    }

    public static BufferedImage copy(BufferedImage bufferedImage) {
        int w = bufferedImage.getWidth(), h = bufferedImage.getHeight();
        int [] pixels = bufferedImage.getRGB(0, 0, w, h, null, 0, w);
        BufferedImage cpBuffer = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        /**
         * TODO percorrer um vetor e inserir os dados numa matriz
         * */
        for(int i=0; i<w*h; i++) {
            int w1 = i%w, h1 = (i/w);
            cpBuffer.setRGB(w1, h1, pixels[i]);
        }
        return cpBuffer;
    }



    public BufferedImage applyMask(int [][] mask, String filename, String format) {
        BufferedImage buffer = bufferedImage;
        int limitI = mask.length, limitJ = mask[0].length;
        // posicao da matrix que presenta o PIXEL de interesse
        int xCenterMask = limitI/2, yCenterMask = limitJ/2;
        int meanMatrix = MaskFilterDefault.mean(mask);
        for(int i=0; i<heightImage-limitJ+1; i++) {
            for (int j=0; j<widthImage-limitI+1; j++) {
                int accR = 0, accG = 0, accB = 0;
                for (int x=0; x<limitI; x++) {
                    for (int y=0; y<limitJ; y++) {
                        int newX = i+x, newY = j+y;
                        int c = matrixPixels[newX][newY];
                        Color color = new Color(c);
                        int r = color.getRed(),g = color.getGreen(), b = color.getBlue();
                        accR += r * mask[x][y];
                        accG += g * mask[x][y];
                        accB += b * mask[x][y];
                    }
                }
                /*
                accR /= meanMatrix;
                accG /= meanMatrix;
                accB /= meanMatrix;
                */
                accR = accR > 255 ? 255 : accR < 0 ? 0 : accR;
                accB = accB > 255 ? 255 : accB < 0 ? 0 : accB;
                accG = accG > 255 ? 255 : accG < 0 ? 0 : accG;
                buffer.setRGB(j+xCenterMask, i+yCenterMask, new Color(accR, accG, accB).getRGB());
            }
        }
        //FiltersToGrayScale.createImage(buffer, String.format("images/after_%s.%s", filename,  format));
        //System.out.println("Imagem criada apos o filtro");
        return buffer;
    }

    /**
     * Metodo responsavel por executar um processo antes de aplicar o filtro
     * aplica o filtro, e executa um processo apos aplicacao do filtro
     *
     * */
    private final void apply(int mask [][], String filename, String format) {
        if(callbackApplyFilter != null)
            callbackApplyFilter.before(bufferedImage);
        BufferedImage buffer = applyMask(mask, filename, format);
        if(callbackApplyFilter != null)
            callbackApplyFilter.after(buffer);
        //bufferedImage = buffer;
        redefineBufferedImage(buffer);
    }

    public BufferedImage applyMeanOp(int typeOfMask, String filename, String format) {
        int mask[][];
        switch (typeOfMask) {
            case 1:
                mask = MaskFilterDefault.MaskToRGB.boxBlur;
                break;
            case 2:
                mask = MaskFilterDefault.MaskToRGB.gaussianBlur3;
                break;
            case 3:
                mask = MaskFilterDefault.MaskToRGB.gaussianBlur5;
                break;
            default:
                mask = MaskFilterDefault.MaskToRGB.boxBlur;
        }

        int meanMatrix = MaskFilterDefault.mean(mask);

        BufferedImage buffer = bufferedImage;
        int limitI = mask.length, limitJ = mask[0].length;
        // posicao da matrix que presenta o PIXEL de interesse
        int xCenterMask = limitI/2, yCenterMask = limitJ/2;

        for(int i=0; i<heightImage-limitJ+1; i++) {
            for (int j=0; j<widthImage-limitI+1; j++) {
                int accR = 0, accG = 0, accB = 0;
                for (int x=0; x<limitI; x++) {
                    for (int y=0; y<limitJ; y++) {
                        int newX = i+x, newY = j+y;
                        int c = matrixPixels[newX][newY];
                        Color color = new Color(c);
                        int r = color.getRed(),g = color.getGreen(), b = color.getBlue();
                        accR += r * mask[x][y];
                        accG += g * mask[x][y];
                        accB += b * mask[x][y];
                    }
                }
                accR /= meanMatrix;
                accG /= meanMatrix;
                accB /= meanMatrix;
                accR = accR > 255 ? 255 : accR < 0 ? 0 : accR;
                accB = accB > 255 ? 255 : accB < 0 ? 0 : accB;
                accG = accG > 255 ? 255 : accG < 0 ? 0 : accG;
                buffer.setRGB(j+xCenterMask, i+yCenterMask, new Color(accR, accG, accB).getRGB());
            }
        }
        //FiltersToGrayScale.createImage(buffer, String.format("images/after_%s.%s", filename,  format));
        System.out.println("Imagem criada apos o filtro");
        return buffer;
    }

    /**
     * Action para deteccao de borda
     * */
    public final ActionListener gradientBorderDetectorHorizontal = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.horizontalGradient, "HorizontalGradientBorderDetector", "jpg");
        }
    };
    public final ActionListener gradientBorderDetectorVertical= new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.verticalGradient, "VerticalGradientBorderDetector", "jpg");
        }
    };
    public final ActionListener sobelHorizontalBorderDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.sobelHorizontal, "SobelHorizontalBorderDetector", "jpg");
        }
    };
    public final ActionListener sobelVerticakBorderDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.sobelVertical, "SobelVerticalBorderDetector", "jpg");
        }
    };
    public final ActionListener prewittHorizontalBorderDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.prewittHorizontal, "PrewiTTHoriziontalBorderDetector", "jpg");
        }
    };
    public final ActionListener prewittVerticalBorderDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskBorderDetector.prewittVertical, "PrewiTTVerticalBorderDetector", "jpg");
        }
    };

    /**
     * Fim das actions para deteccao de borta
     * */


    /**
     * Actions para deteccao de linhas
     * */

    public final ActionListener horizontalLineDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskLineDetection.maskHorinzontal, "Deteccao Horizonal", "jpg");
        }
    };
    public final ActionListener verticalLineDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskLineDetection.maskVertical, "Deteccao Vertical", "jpg");
        }
    };
    public final ActionListener p45degreeLineDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskLineDetection.maskP45, "Deteccao De linhas p45grau", "jpg");
        }
    };
    public final ActionListener m45degreeLineDetection = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskLineDetection.maskM45, "Deteccao De linhas m45grau", "jpg");
        }
    };

    /**
     * Fim das actions de detecacao de linhas
     * */
    /**/
    public final ActionListener laplacianFilter = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskToRGB.laplacian, "filtro_laplacian", "jpg");
        }
    };

    public final ActionListener passaAltaFilter = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.MaskToRGB.passaAlta, "filtro_laplacian", "jpg");
        }
    };

    public final ActionListener mean9 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.meanFilter9, "filtro_mean9", "jpg");
        }
    };

    public final ActionListener mean16 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            apply(MaskFilterDefault.meanFilter16, "filtro_mean16", "jpg");
        }
    };


    public final ActionListener gaussianBlue3 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(callbackApplyFilter != null)
                callbackApplyFilter.before(bufferedImage);
            BufferedImage buffer = applyMeanOp(2, "filtro_gaussiano3por3", "jpg");
            if(callbackApplyFilter != null)
                callbackApplyFilter.after(buffer);
            redefineBufferedImage(buffer);
        }
    };

    public BufferedImage matrixToBuffer(int matrix[][]) {
        int w = matrix[0].length, h = matrix.length;
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for(int i=0; i<h; i++) {
            for (int j = 0; j < w; j++) {
                int c = matrix[i][j];
                Color color = new Color(c,c,c);
                buffer.setRGB(j, i, color.getRGB());
            }
        }
        return  buffer;
    }

    public int [][] matrixGrayScale(int [][] matrix) {
        int w = matrix[0].length, h = matrix.length;
        int [][] mat = new int[h][w];
        for(int i=0; i<h; i++) {
            for(int j=0; j<w; j++) {
                Color color = new Color(matrix[i][j]);
                int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                mat[i][j] = (r+b+g)/3;
            }
        }
        return mat;
    }

    public final class Splitting implements ActionListener {
        private int maxGrayScale, interval, constant;
        private CallbackApplyFilter callbackApplyFilter;

        public Splitting(CallbackApplyFilter callbackApplyFilter, int interval, int maxGrayScale, int constant) {
            this.interval       = interval;
            this.maxGrayScale   = maxGrayScale;
            this.constant       = constant;
            this.callbackApplyFilter = callbackApplyFilter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int [][] matrixGS = matrixGrayScale(getMatrixPixels());
            int w = matrixGS[0].length, h = matrixGS.length;

            for (int i = 0; i <h ; i++) {
                for (int j = 0; j<w ; j++) {
                    int c = matrixGS[i][j];
                    matrixGS[i][j] += (this.maxGrayScale / interval) > c ? constant : (-constant);
                }
            }

            if(callbackApplyFilter != null) {
                BufferedImage buffer = matrixToBuffer(matrixGS);
                callbackApplyFilter.after(buffer);
            }
        }
    }

    private final ArrayList<Color> getNeighboors(int matrix [][], int quantity) {
        ArrayList<Color> colors = new ArrayList<>();
        switch (quantity) {
            case 4:
                break;
            case 8:
                break;
        }
        return colors;
    }

    public final ActionListener medianFilter4 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage buffer = new BufferedImage(widthImage, heightImage, BufferedImage.TYPE_INT_RGB);
            ArrayList<Color> colors;
            int matrixGC [][] = matrixGrayScale(matrixPixels);
            for(int i=1; i<heightImage-1; i++) {
                for (int j=1; j<widthImage-1; j++) {
                    colors = new ArrayList<>();
                    if(i>0) {
                        int c = matrixGC[i-1][j];
                        colors.add(new Color(c, c, c)/*new Color(bufferedImage.getRGB(i-1, j))*/); // cima
                    }
                    if(j<widthImage-1) {
                        int c = matrixGC[i][j+1];
                        colors.add(new Color(c, c, c)/*new Color(bufferedImage.getRGB(i, j + 1))*/); // direita
                    }
                    if(i<heightImage-1) {
                        int c = matrixGC[i+1][j];
                        colors.add(new Color(c, c, c)/*new Color(bufferedImage.getRGB(i + 1, j))*/); // embaixo
                    }
                    if(j>0) {
                        int c = matrixGC[i][j-1];
                        colors.add(new Color(c, c, c)/*new Color(bufferedImage.getRGB(i, j - 1))*/); // esquerda
                    }
                    int sz = colors.size();
                    int hl = sz / 2;
                    int r [] = new int [sz];
                    int g [] = new int [sz];
                    int b [] = new int [sz];
                    int x=0;
                    for(Color color : colors) {
                        r[x] = color.getRed();
                        g[x] = color.getGreen();
                        b[x] = color.getBlue();
                        x++;
                    }
                    Arrays.sort(r);
                    Arrays.sort(g);
                    Arrays.sort(b);
                    buffer.setRGB(j,i,new Color(r[hl],g[hl],b[hl]).getRGB());
                }
            }
            if(callbackApplyFilter != null)
                callbackApplyFilter.after(buffer);
            //bufferedImage = buffer;
            redefineBufferedImage(buffer);
        }
    };

    public final class EqualizationInGrayScale implements ActionListener {
        private int quantityGrayScaleLevel = 10;
        private int[] histogram;
        private CallbackApplyFilter callbackApplyFilter;
        private int [][] matrixGrayScalePixels;
        public EqualizationInGrayScale(int quantityGrayScaleLevel, int[] histogram, CallbackApplyFilter callbackApplyFilter) {
            // quantidade de niveis de cinza
            this.quantityGrayScaleLevel = quantityGrayScaleLevel;
            this.histogram              = histogram;
            this.callbackApplyFilter    = callbackApplyFilter;
            // convertendo a matriz de uma imagem em RGB para uma matriz em nivel de cinza
            this.matrixGrayScalePixels  = matrixGrayScale(matrixPixels);
        }

        public void redefine(int matrixPixels [][]) {
            this.matrixGrayScalePixels  = matrixGrayScale(matrixPixels);
        }

        public int getQuantityGrayScaleLevel() {
            return  this.quantityGrayScaleLevel;
        }

        public int[] getMatrixHistogramGrayScale(int [] pixelsImage, int h, int w) {
            int [] histogramGrayScale = new int[256];
            for(int i=0; i<h; i++){ // linha
                for(int j=0; j<w; j++) {    // coluna
                    Color color = new Color(pixelsImage[i*w+j]);
                    int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                    int l = (r+g+b)/3;
                    l = l > 255 ? 255 : l < 0 ? 0 : l;
                    histogramGrayScale[l] += 1;
                }
            }
            return histogramGrayScale ;
        }

        public int [] getAccumulateHistogram() {
            int [] accHistogram = new int [histogram.length];
            accHistogram[0] = histogram[0];
            for(int i=1; i<histogram.length; i++) {
                accHistogram[i] = accHistogram[i-1] + histogram[i];
            }
            return accHistogram;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int quantityGrayScaleLevel  = getQuantityGrayScaleLevel();
            /**
             * Quantidade de pixels ideais
             * */
            int quantityIdealPixels = widthImage * heightImage / quantityGrayScaleLevel;
            int accHistogram [] = getAccumulateHistogram();
            int q[] = new int [accHistogram.length];
            /**
             * Geramdo vetor Q []
             * A partir desse novo vetor podemos modificar os valores dos pixels
             * da imagem original afim de equalizar os valores dos tons de cinza
             * */
            for(int i=0; i<accHistogram.length; i++) {
                int v = accHistogram[i]/quantityIdealPixels - 1;
                q[i] = 0 > v ? 0 : v;
            }
            int [][] newMatrixPixels = new int[heightImage][widthImage];
            for(int i=0; i<heightImage; i++) {
                for (int j = 0; j < widthImage; j++) {
                    // criando uma nova imagem a partir do vetor Q
                    newMatrixPixels[i][j] = q[matrixGrayScalePixels[i][j]];
                }
            }

            BufferedImage buffer = matrixToBuffer(newMatrixPixels);
            if(callbackApplyFilter != null) {
                // redesenhado a imagem no canvas
                callbackApplyFilter.after(buffer);
            }

            redefineBufferedImage(buffer);
            int w = buffer.getWidth(), h = buffer.getHeight();
            this.histogram = getMatrixHistogramGrayScale(buffer.getRGB(0, 0, w, h, null, 0, w), h, w);
            // imagem antiga recebe o valor novo
            // matrixGrayScalePixels = newMatrixPixels;
        }
    }

    public final class QuantizationInGrayScale implements  ActionListener {

        /**
         *
         * Esse algoritmo me permite gerar um histograma com uma quantidade especifica de niveis de cinza.
         *
         * Ao inves de gerar um histograma com niveis de cinda de 0 a 255, faço de 0 a 'quantityLevels'
         * Funciona da seguinte forma
         *
         * Se quantityLevels = 10 e o valor maximo de um pixel (maxScale) for = 255
         * queremos que os valores de 0 a 255 se encaixem entre 0 e 10 precisamos fazer o seguinte
         *
         * sets = 255/10 -> descobrinmos que em cada quantidade de pixels temos a´rpximadamente 25 cores
         * [0] -> 0 - 24
         * [1] -> 25 - 49
         * [2] -> 50 - 74, son on
         *
         * Entao para uma cor C, basta dividir essa cor por 25 e sabemos onde encaixa-la no vetor
         *
         *
         * */
        public int[] getMatrixHistogramGrayScale(int [] pixelsImage, int h, int w, int maxScale, int quantityLevels) {
            int [] histogramGrayScale = new int[quantityLevels];
            int sets = maxScale/quantityLevels;
            for(int i=0; i<h; i++){ // linha
                for(int j=0; j<w; j++) {    // coluna
                    Color color = new Color(pixelsImage[i*w+j]);
                    int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                    int l = r+g+b/3;
                    l = l > 255 ? 0 : l < 0 ? 0 : l;
                    histogramGrayScale[l/sets] += 1;
                }
            }
            return histogramGrayScale ;
        }
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    public static final void createImage(BufferedImage buffer, String pathfile, String format) {
        File outputFile = new File(pathfile);
        if( ! outputFile.exists() ) {
            String path = outputFile.getParent();
            new File(path).mkdirs();
        }
        try {
            ImageIO.write(buffer, format, outputFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
