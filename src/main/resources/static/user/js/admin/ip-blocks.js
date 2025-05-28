let countryChart, typeChart;
let lastFetchTime = null;

function updateStats(data) {
    const totalBlocked = data.length;
    const botCount = data.filter(log => log.bot).length;
    const countries = new Set(data.map(log => log.country || 'Unknown')).size;

    document.getElementById('totalBlocked').textContent = totalBlocked.toLocaleString();
    document.getElementById('botCount').textContent = botCount.toLocaleString();
    document.getElementById('countryCount').textContent = countries.toLocaleString();
    document.getElementById('lastUpdate').textContent = new Date().toLocaleTimeString();
}

function formatUserAgent(userAgent) {
    if (!userAgent) return 'N/A';
    if (userAgent.length > 50) {
        return userAgent.substring(0, 50) + '...';
    }
    return userAgent;
}

function formatTime(timeString) {
    try {
        return new Date(timeString).toLocaleString('ko-KR');
    } catch {
        return timeString;
    }
}

async function fetchBlockedIps() {
    try {
        const statusIndicator = document.getElementById('statusIndicator');

        const res = await fetch("/api/admin/blocked-ips");
        const data = await res.json();

        statusIndicator.className = 'status-indicator online';
        statusIndicator.innerHTML = '<div class="pulse"></div>실시간 모니터링 중';

        updateStats(data);

        const tbody = document.getElementById("logTableBody");
        tbody.innerHTML = "";

        if (data.length === 0) {
            tbody.innerHTML = `
                        <tr>
                            <td colspan="6" class="empty-state">
                                <i class="fas fa-shield-alt"></i>
                                <div>차단된 IP가 없습니다</div>
                            </td>
                        </tr>
                    `;
        } else {
            const countryCounts = {};
            let botCount = 0, browserCount = 0;

            data.forEach(log => {
                const row = `
                        <tr>
                            <td class="ip-cell">${log.ip}</td>
                            <td class="country-cell">
                                <div class="country-flag"></div>
                                ${log.country || 'N/A'}
                            </td>
                            <td>${log.city || 'N/A'}</td>
                            <td title="${log.userAgent || 'N/A'}">${formatUserAgent(log.userAgent)}</td>
                            <td>
                                <span class="bot-badge ${log.bot ? 'bot' : 'browser'}">
                                    ${log.bot ? 'Bot' : 'Browser'}
                                </span>
                            </td>
                            <td class="time-cell">${formatTime(log.blockedAt)}</td>
                        </tr>`;
                tbody.insertAdjacentHTML("beforeend", row);

                const country = log.country || "Unknown";
                countryCounts[country] = (countryCounts[country] || 0) + 1;
                log.bot ? botCount++ : browserCount++;
            });

            updateCharts(countryCounts, botCount, browserCount);
        }
    } catch (error) {
        const statusIndicator = document.getElementById('statusIndicator');
        statusIndicator.className = 'status-indicator offline';
        statusIndicator.innerHTML = '<div class="pulse"></div>연결 실패';
    }
}

function updateCharts(countryCounts, botCount, browserCount) {
    const countries = Object.keys(countryCounts);
    const counts = Object.values(countryCounts);

    // Country Chart
    if (!countryChart) {
        countryChart = new Chart(document.getElementById('countryChart'), {
            type: 'doughnut',
            data: {
                labels: countries,
                datasets: [{
                    label: 'Blocked IPs by Country',
                    data: counts,
                    backgroundColor: [
                        '#6366f1', '#8b5cf6', '#06b6d4', '#10b981',
                        '#f59e0b', '#ef4444', '#ec4899', '#84cc16'
                    ],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    } else {
        countryChart.data.labels = countries;
        countryChart.data.datasets[0].data = counts;
        countryChart.update();
    }

    // Type Chart
    if (!typeChart) {
        typeChart = new Chart(document.getElementById('typeChart'), {
            type: 'doughnut',
            data: {
                labels: ['Bot', 'Browser'],
                datasets: [{
                    data: [botCount, browserCount],
                    backgroundColor: ['#ef4444', '#10b981'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    } else {
        typeChart.data.datasets[0].data = [botCount, browserCount];
        typeChart.update();
    }
}

// 초기 로딩 및 주기적 업데이트
setInterval(fetchBlockedIps, 5000);
fetchBlockedIps();