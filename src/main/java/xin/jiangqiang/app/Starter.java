package xin.jiangqiang.app;

/**
 * 项目启动类接口
 *
 * @author jiangqiang
 * @date 2020/12/17 20:15
 */
public interface Starter {
    void start();

    /**
     * 程序没有正常执行完时会执行此方法
     */
    void clearResource();
}
