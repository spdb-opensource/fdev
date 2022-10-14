package common

type Executor interface {

	//准备阶段，举个例子，对于在k8s运行的job，准备阶段需要连接k8s并初始化pod
	Prepare(job *Job) error

	//运行阶段，主要逻辑在这里面。会在运行时依次下载插件并执行
	Run() error

	//等待job执行完成
	Wait() error

	//job执行失败则发送服务端
	SendError(err error)

	//任务执行结束之后，进行一些收尾工作。比如说删除用于执行任务的临时的k8s Pod,断开k8s连接等
	Cleanup() error
}

type BuildError struct {
	Inner error
}

func (b *BuildError) Error() string {
	if b.Inner == nil {
		return "error"
	}

	return b.Inner.Error()
}

type ExecutorProvider interface {
	CanCreate() bool
	Create() Executor
}

var executors map[string]ExecutorProvider

//获取executor
func GetExecutor(executorStr string) ExecutorProvider {
	if executors == nil {
		return nil
	}
	return executors[executorStr]
}

//注册executor
func RegisterExecutor(executor string, provider ExecutorProvider) {
	if executors == nil {
		executors = make(map[string]ExecutorProvider)
	}
	if _, ok := executors[executor]; ok {
		panic("Executor already exist: " + executor)
	}
	executors[executor] = provider
}

//func RegisterExecutor(executorStr string, executor Executor) {
//	logrus.Debugln("Registering", executor, "executor...")
//	if executors == nil {
//		executors = make(map[string]Executor)
//	}
//	if _, ok := executors[executorStr]; ok {
//		logrus.Panicln("Executor already exist: " + executorStr)
//	}
//	executors[executorStr] = executor
//}
