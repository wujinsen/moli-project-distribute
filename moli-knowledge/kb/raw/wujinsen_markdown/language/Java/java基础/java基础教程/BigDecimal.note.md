/ BigDecimal heightM = new BigDecimal(10).divide(new BigDecimal(10), 2,BigDecimal.ROUND_HALF_DOWN); / System.out.println(heightM); / / 2位 ⼩ 数 / BigDecimal square = heightM.multiply(heightM); / / 按 2位 ⼩ 数 计 算 结 果 / BigDecimal bmi = new BigDecimal(80).divide(square, 2, BigDecimal.ROUND_HALF_DOWN); / System.out.println(bmi); / System.out.println(new BigDecimal(80).divide(new BigDecimal(4), 2, BigDecimal.ROUND_HALF_DOWN);

System.out.println(18.5*1.70*1.70);

BigDecimal heightM =newBigDecimal(170).divide(newBigDecimal(10),2,BigDecimal.ROUND_HALF_DOWN); BigDecimal bmiM =newBigDecimal(18.5).divide(newBigDecimal(1),2,BigDecimal.ROUND_HALF_DOWN);

/ 2位 ⼩ 数

BigDecimal square = heightM.multiply(heightM).multiply(bmiM);

/ 按 2位 ⼩ 数 计 算 结 果

BigDecimal weightAvg =newBigDecimal(square.doubleValue().divide(newBigDecimal(1),2, BigDecimal.ROUND_HALF_DOWN);

System.out.println(weightAvg.doubleValue();

