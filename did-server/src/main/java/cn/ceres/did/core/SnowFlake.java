package cn.ceres.did.core;

/**
 * twitter 的 snowflake 算法 -- java 实现
 * <p>
 * SnowFlake 的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生 ID 碰撞（由数据中心 ID 和机器 ID 作区分），并且效率较高，经测试，SnowFlake 每秒能够产生 26 万 ID 左右。
 * <p>
 * 协议格式：0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 协议解释：0 - 41 位时间戳 - 5 位数据中心标识 - 5 位机器标识 - 12 位序列号
 * <p>
 * 1 位标识，由于 long 基本类型在 Java 中是带符号的，最高位是符号位，正数是 0，负数是 1，所以 id 一般是正数，最高位是 0
 * <p>
 * 41 位时间截（毫秒级），注意，41 位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)得到的值，这里的的开始时间截。
 * 一般是我们的 id 生成器开始使用的时间，由我们程序来指定的（如下下面程序 START_STMP 属性）。41 位的时间截，可以使用 69 年，(1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * <p>
 * 10 位的数据机器位，可以部署在 1024 个节点，包括 5 位数据中心标识，5 位机器标识
 * <p>
 * 12 位序列，毫秒内的计数，12 位的计数顺序号支持每个节点每毫秒（同一机器，同一时间截）产生 4096 个 ID 序号，加起来刚好 64 位，为一个 Long 型。
 *
 * @author ehlxr
 */
public class SnowFlake {
    /**
     * 起始的时间戳，可以修改为服务第一次启动的时间
     * 一旦服务已经开始使用，起始时间戳就不能改变
     * <p>
     * 2018/8/14 00:00:00
     */
    private final static long START_STMP = 1534176000000L;

    /**
     * 序列号占用的位数
     */
    private final static long SEQUENCE_BIT = 12;
    /**
     * 机器标识占用的位数
     */
    private final static long MACHINE_BIT = 5;
    /**
     * 数据中心占用的位数
     */
    private final static long DATACENTER_BIT = 5;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    /**
     * 数据中心
     */
    private final long datacenterId;
    /**
     * 机器标识
     */
    private final long machineId;
    /**
     * 序列号
     */
    private long sequence = 0L;
    /**
     * 上一次时间戳
     */
    private long lastStmp = -1L;


    /**
     * 通过单例模式来获取实例
     * 分布式部署服务时，数据节点标识和机器标识作为联合键必须唯一
     *
     * @param datacenterId 数据节点标识ID
     * @param machineId    机器标识ID
     */
    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        // 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
        return (currStmp - START_STMP) << TIMESTMP_LEFT | datacenterId << DATACENTER_LEFT | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 3);
        long start = System.currentTimeMillis();
        for (int i = 0; i < (1 << 18); i++) {
            System.out.println(i + ": " + snowFlake.nextId());
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
