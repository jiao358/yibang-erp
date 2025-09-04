// AI Excel缓存功能测试脚本
// 在浏览器控制台中运行此脚本来测试缓存功能

console.log('🧪 开始测试AI Excel缓存功能...');

// 模拟缓存任务数据
const testCachedTask = {
  taskId: 'test-task-' + Date.now(),
  fileName: '测试文件.xlsx',
  status: 'SYSTEM_PROCESSING',
  totalRows: 100,
  successRows: 20,
  failedRows: 0,
  manualProcessRows: 0,
  createdAt: new Date().toISOString(),
  fileSize: 1024000,
  uploadUser: '测试用户',
  supplier: '系统处理中',
  isCached: true
};

// 测试1: 创建缓存任务
function testCreateCache() {
  console.log('📝 测试1: 创建缓存任务');
  
  const CACHED_TASKS_KEY = 'aiExcelCachedTasks';
  const arr = JSON.parse(localStorage.getItem(CACHED_TASKS_KEY) || '[]');
  arr.unshift(testCachedTask);
  localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(arr));
  
  console.log('✅ 缓存任务已创建:', testCachedTask.taskId);
  return testCachedTask.taskId;
}

// 测试2: 加载缓存任务
function testLoadCache() {
  console.log('📋 测试2: 加载缓存任务');
  
  const CACHED_TASKS_KEY = 'aiExcelCachedTasks';
  const raw = localStorage.getItem(CACHED_TASKS_KEY);
  const arr = JSON.parse(raw || '[]');
  
  console.log('📋 找到缓存任务数量:', arr.length);
  arr.forEach((task, index) => {
    console.log(`  任务 ${index + 1}: ${task.taskId} - ${task.fileName} (${task.status})`);
  });
  
  return arr;
}

// 测试3: 模拟任务历史合并
function testMergeTasks() {
  console.log('🔄 测试3: 模拟任务历史合并');
  
  // 模拟真实任务数据
  const realTasks = [
    {
      taskId: 'real-task-001',
      fileName: '真实任务1.xlsx',
      status: 'COMPLETED',
      totalRows: 50,
      successRows: 45,
      failedRows: 5,
      createdAt: new Date().toISOString()
    }
  ];
  
  // 加载缓存任务
  const cachedTasks = testLoadCache();
  const existingIds = new Set(realTasks.map(t => t.taskId));
  const validCachedTasks = cachedTasks.filter(t => !existingIds.has(t.taskId));
  
  // 合并任务
  const mergedTasks = [...validCachedTasks, ...realTasks];
  
  console.log('🔄 合并结果:');
  console.log('  真实任务:', realTasks.length);
  console.log('  有效缓存任务:', validCachedTasks.length);
  console.log('  总任务数:', mergedTasks.length);
  
  mergedTasks.forEach((task, index) => {
    const type = task.isCached ? '缓存' : '真实';
    console.log(`  ${index + 1}. [${type}] ${task.taskId} - ${task.fileName}`);
  });
  
  return mergedTasks;
}

// 测试4: 清理缓存任务
function testCleanCache(taskId) {
  console.log('🗑️ 测试4: 清理缓存任务');
  
  const CACHED_TASKS_KEY = 'aiExcelCachedTasks';
  const arr = JSON.parse(localStorage.getItem(CACHED_TASKS_KEY) || '[]');
  const filtered = arr.filter(t => t.taskId !== taskId);
  
  if (filtered.length !== arr.length) {
    localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(filtered));
    console.log(`✅ 已清理缓存任务: ${taskId}`);
    console.log(`📋 剩余缓存任务: ${filtered.length}`);
  } else {
    console.log('⚠️ 未找到要清理的缓存任务');
  }
}

// 测试5: 过期任务清理
function testExpiredCleanup() {
  console.log('⏰ 测试5: 过期任务清理');
  
  const CACHED_TASKS_KEY = 'aiExcelCachedTasks';
  const arr = JSON.parse(localStorage.getItem(CACHED_TASKS_KEY) || '[]');
  
  // 创建一个过期的任务（2小时前）
  const expiredTask = {
    ...testCachedTask,
    taskId: 'expired-task-' + Date.now(),
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() // 2小时前
  };
  
  arr.push(expiredTask);
  localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(arr));
  
  console.log('📋 添加过期任务前:', arr.length);
  
  // 模拟过期清理逻辑
  const now = new Date().getTime();
  const validTasks = arr.filter(task => {
    const taskTime = new Date(task.createdAt).getTime();
    const hoursDiff = (now - taskTime) / (1000 * 60 * 60);
    return hoursDiff < 1; // 保留1小时内的任务
  });
  
  if (validTasks.length !== arr.length) {
    localStorage.setItem(CACHED_TASKS_KEY, JSON.stringify(validTasks));
    console.log(`✅ 清理了 ${arr.length - validTasks.length} 个过期任务`);
  }
  
  console.log('📋 清理后剩余任务:', validTasks.length);
}

// 运行所有测试
function runAllTests() {
  console.log('🚀 开始运行所有测试...');
  
  try {
    const taskId = testCreateCache();
    testLoadCache();
    testMergeTasks();
    testCleanCache(taskId);
    testExpiredCleanup();
    
    console.log('✅ 所有测试完成！');
  } catch (error) {
    console.error('❌ 测试失败:', error);
  }
}

// 清理所有测试数据
function cleanupTests() {
  console.log('🧹 清理测试数据...');
  localStorage.removeItem('aiExcelCachedTasks');
  console.log('✅ 测试数据已清理');
}

// 显示使用说明
console.log(`
📖 使用说明:
1. runAllTests() - 运行所有测试
2. testCreateCache() - 创建测试缓存任务
3. testLoadCache() - 加载缓存任务
4. testMergeTasks() - 测试任务合并
5. testCleanCache(taskId) - 清理指定缓存任务
6. testExpiredCleanup() - 测试过期清理
7. cleanupTests() - 清理所有测试数据

示例: runAllTests()
`);

// 自动运行测试
runAllTests();
